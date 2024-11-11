package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.Invitation;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.login.UserRepository;
import sit.int221.integratedapi.repositories.primary.CollaboratorRepository;
import sit.int221.integratedapi.repositories.primary.InvitationRepository;
import sit.int221.integratedapi.services.*;
import sit.int221.integratedapi.utils.ListMapper;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class CollaboratorController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private InvitationRepository invitationRepository;

//    @GetMapping("/{boardId}/collabs")
//    public ResponseEntity<Map<String, Object>> getCollaboratorsByBoardId(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId) {
//
//        String oidFromToken = null;
//
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//
//        // ตรวจสอบสิทธิ์การเข้าถึงบอร์ด
//        boardService.validateBoardAccessForGet(boardId, oidFromToken);
//
//        // ดึงข้อมูล collaborators และแปลงเป็น response object
//        Map<String, Object> collaboratorsResponse = collaboratorService.getCollaboratorsByBoardId(boardId, oidFromToken);
//
//        return ResponseEntity.ok(collaboratorsResponse);
//    }

    @GetMapping("/{boardId}/collabs")
    public ResponseEntity<CollaboratorListDTO> getCollaboratorsAndPendingByBoardId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId) {

        String oidFromToken = null;

        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }

        // Validate board access for the user
        boardService.validateBoardAccessForGet(boardId, oidFromToken);

        // Retrieve collaborators and pending invitations
        CollaboratorListDTO collaboratorsResponse = collaboratorService.getCollaboratorsAndPendingByBoardId(boardId,oidFromToken);

        return ResponseEntity.ok(collaboratorsResponse);
    }




    @GetMapping("/{boardId}/collabs/{collabOid}")
    public ResponseEntity<CollaboratorDTO> getCollaboratorById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable String collabOid) {

        String oidFromToken = null;

        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }

        boardService.validateBoardAccessForGet(boardId, oidFromToken);

        CollaboratorDTO collaborator = collaboratorService.getCollaboratorById(boardId, collabOid, oidFromToken);
        if (collaborator == null) {
            throw new ItemNotFoundException("Collaborator not found");
        }

        return ResponseEntity.ok(collaborator);
    }


    @GetMapping("/{boardId}/collabs/invitations")
    public ResponseEntity<?> getUserInvitations(
            @PathVariable("boardId") String boardId,
            @RequestHeader(value = "Authorization", required = false) String token) {

        // Check if the token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is required.");
        }

        // Extract OID from the token
        String userOid = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));

        // Fetch the pending invitation for the signed-in user for the specified board
        InvitationDTO invitation = collaboratorService.getPendingInvitationForUser(boardId, userOid);

        return ResponseEntity.ok(invitation);
    }







    @PostMapping("/{boardId}/collabs")
    public ResponseEntity<?> addCollaborator(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @RequestBody(required = false) CollaboratorCDTO collaboratorCDTO) {

        // Step 1: Check if authorization token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is required.");
        }

        // Step 2: Extract the oid from the token
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));

        // Step 3: Validate write access to the board
        collaboratorService.validateWriteAccess(boardId, oidFromToken);

        // Step 4: Check if accessRight is provided and valid
        if (collaboratorCDTO.getAccessRight() == null || collaboratorCDTO.getAccessRight().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Access right must be provided and cannot be null or empty.");
        }

        Collaborator.AccessRight accessRightEnum;
        try {
            accessRightEnum = Collaborator.AccessRight.valueOf(collaboratorCDTO.getAccessRight());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid access right provided. Valid values are READ or WRITE.");
        }

        // Step 5: Validate the email of the collaborator
        User user = userRepository.findByEmail(collaboratorCDTO.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + collaboratorCDTO.getEmail());
        }

        // Step 6: Check if the board owner is trying to add themselves as a collaborator
        if (collaboratorService.isBoardOwner(boardId, oidFromToken) &&
                collaboratorService.isEmailOfBoardOwner(boardId, collaboratorCDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Board owner cannot be a collaborator of his/her own board.");
        }

        // Step 7: Check if the user is already a collaborator or has a pending invitation
        if (collaboratorService.isUserAlreadyCollaborator(boardId, collaboratorCDTO.getEmail()) ||
                collaboratorService.hasPendingInvitation(boardId, collaboratorCDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("The user is already a collaborator or has a pending invitation for this board.");
        }

        // Step 8: Add the invitation (not directly adding the collaborator)
        InvitationCDTO newInvitation = collaboratorService.addCollaboratorByEmail(
                boardId,
                collaboratorCDTO.getEmail(),
                accessRightEnum.name(),
                oidFromToken,
                user.getName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(newInvitation);

    }


    @PostMapping("/{boardId}/collabs/invitations/{invitationId}/confirm")
    public ResponseEntity<?> confirmInvitation(@PathVariable("boardId") String boardId,
                                               @PathVariable("invitationId") Integer invitationId,
                                               @RequestParam("accept") boolean acceptInvitation,
                                               @RequestHeader(value = "Authorization", required = false) String token) {

        // Step 1: Check if the authorization token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is required.");
        }

        // Step 2: Extract the oid from the token
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));

        if (oidFromToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
        }

        // Step 3: Fetch the invitation from the database
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ItemNotFoundException("Invitation not found"));

        // Step 4: Validate that the invitation is for the correct board
        if (!invitation.getBoard().getId().equals(boardId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invitation does not belong to the specified board.");
        }

        // Step 5: Confirm or decline the invitation
        collaboratorService.confirmCollaborator(invitationId, acceptInvitation, oidFromToken);

        if (acceptInvitation) {
            return ResponseEntity.ok("Invitation accepted successfully.");
        } else {
            return ResponseEntity.ok("Invitation declined successfully.");
        }
    }







    @PatchMapping("/{boardId}/collabs/{collabOid}")
    public ResponseEntity<?> updateCollaboratorAccessRight(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable String collabOid,
            @RequestBody(required = false) CollaboratorCDTO collaboratorCDTO) {

        // Step 1: Check if authorization token is provided
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is required.");
        }

        // Step 2: Extract the oid from the token
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));

        // Step 3: Validate write access to the board
        collaboratorService.validateWriteAccess(boardId, oidFromToken);

        // Step 4: Check if accessRight is provided and valid
        if (collaboratorCDTO.getAccessRight() == null || collaboratorCDTO.getAccessRight().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Access right must be provided and cannot be null or empty.");
        }

        Collaborator.AccessRight accessRightEnum;
        try {
            accessRightEnum = Collaborator.AccessRight.valueOf(collaboratorCDTO.getAccessRight());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid access right provided. Valid values are READ or WRITE.");
        }

        // Step 5: Check if the collaborator exists
        User collaboratorUser = userRepository.findByOid(collabOid);
        if (collaboratorUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Collaborator with id " + collabOid + " not found.");
        }

        // Step 6: Check if the collaborator already has the requested access right
        Collaborator existingCollaborator = collaboratorRepository.findByUserOidAndBoardId(collabOid, boardId);
        if (existingCollaborator == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Collaborator not found for this board.");
        }
        if (existingCollaborator.getAccessRight() == accessRightEnum) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Collaborator already has the specified access right.");
        }

        // Step 7: Update the collaborator's access right
        CollaboratorDTO updatedCollaborator = collaboratorService.updateCollaboratorAccessRight(
                boardId, collabOid, accessRightEnum.name(), oidFromToken);

        return ResponseEntity.ok(updatedCollaborator);
    }



    @DeleteMapping("/{boardId}/collabs/{collabOid}")
    public ResponseEntity<?> removeCollaborator(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable String collabOid) {

        // Check if token is null or empty and return 401 Unauthorized
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is required.");
        }

        // Extract the oid from the token
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));



        // Attempt to remove the collaborator and handle any exceptions
        CollaboratorDTO removedCollaborator = collaboratorService.removeCollaboratorFromBoard(boardId, collabOid, oidFromToken);

        // Return 200 OK with the removed collaborator details
        return ResponseEntity.ok(removedCollaborator);
    }




}
