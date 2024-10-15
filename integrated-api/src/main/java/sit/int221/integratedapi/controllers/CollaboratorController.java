package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.login.UserRepository;
import sit.int221.integratedapi.services.*;
import sit.int221.integratedapi.utils.ListMapper;
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

    @GetMapping("/{boardId}/collabs")
    public ResponseEntity<Map<String, Object>> getCollaboratorsByBoardId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId) {

        String oidFromToken = null;

        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }

        // ตรวจสอบสิทธิ์การเข้าถึงบอร์ด
        boardService.validateBoardAccessForGet(boardId, oidFromToken);

        // ดึงข้อมูล collaborators และแปลงเป็น response object
        Map<String, Object> collaboratorsResponse = collaboratorService.getCollaboratorsByBoardId(boardId, oidFromToken);

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

    @PostMapping("/{boardId}/collabs")
    public ResponseEntity<?> addCollaborator(
            @RequestHeader(value = "Authorization",required = false) String token,
            @PathVariable String boardId,
            @RequestBody(required = false) CollaboratorCDTO collaboratorCDTO) {

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authorization token is required.");
        }

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));

        collaboratorService.validateWriteAccess(boardId, oidFromToken);
        if (collaboratorCDTO.getAccessRight() == null || collaboratorCDTO.getAccessRight().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Access right must be provided and cannot be null or empty");
        }
        Collaborator.AccessRight accessRightEnum;
        try {
            accessRightEnum = Collaborator.AccessRight.valueOf(collaboratorCDTO.getAccessRight()); // Try to convert string to enum
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid access right provided. Valid values are READ or WRITE."); // Custom message
        }
        // ค้นหา User โดยใช้อีเมล
        User user = userRepository.findByEmail(collaboratorCDTO.getEmail());
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found with email: " + collaboratorCDTO.getEmail());
        }

        // Check if the board owner is trying to add themselves as a collaborator
        if (collaboratorService.isBoardOwner(boardId, oidFromToken) &&
                collaboratorService.isEmailOfBoardOwner(boardId, collaboratorCDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Board owner cannot be a collaborator of his/her own board.");
        }



        // Check if the user is already a collaborator
        if (collaboratorService.isUserAlreadyCollaborator(boardId, collaboratorCDTO.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("The user is already a collaborator of this board.");
        }

        CollaboratorDTO newCollaborator = collaboratorService.addCollaboratorByEmail(boardId,
                collaboratorCDTO.getEmail(), accessRightEnum.name(), oidFromToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(newCollaborator);
    }








    @PatchMapping("/{boardId}/collabs/{collabOid}")
    public ResponseEntity<CollaboratorDTO> updateCollaboratorAccessRight(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable String boardId,
            @PathVariable String collabOid,
            @RequestBody CollaboratorCDTO collaboratorCDTO) {

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));

        CollaboratorDTO updatedCollaborator = collaboratorService.updateCollaboratorAccessRight(
                boardId, collabOid, collaboratorCDTO.getAccessRight(), oidFromToken);

        return ResponseEntity.ok(updatedCollaborator);
    }

    @DeleteMapping("/{boardId}/collabs/{collabOid}")
    public ResponseEntity<CollaboratorDTO> removeCollaborator(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable String boardId,
            @PathVariable String collabOid) {

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));


        CollaboratorDTO removedCollaborator = collaboratorService.removeCollaboratorFromBoard(boardId, collabOid, oidFromToken);


        return ResponseEntity.ok(removedCollaborator);
    }



}
