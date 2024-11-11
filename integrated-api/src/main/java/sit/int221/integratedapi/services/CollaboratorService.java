package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.Invitation;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.ConflictException;
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.login.UserRepository;
import sit.int221.integratedapi.repositories.primary.CollaboratorRepository;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.InvitationRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollaboratorService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private  EmailService emailService;


    public boolean isUserAlreadyCollaborator(String boardId, String userOid) {
        return collaboratorRepository.existsByBoardIdAndUserOid(boardId, userOid);
    }


    public boolean isBoardOwner(String boardId, String userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));
        return board.getOwnerId().equals(userId);
    }

    public boolean isEmailOfBoardOwner(String boardId, String email) {
        // Get the user associated with the email
        User user = userRepository.findByEmail(email);

        // Get the oid of the board owner
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
        String boardOwnerOid = board.getOwnerId();

        // Compare the oids
        return user.getOid().equals(boardOwnerOid);
    }


    public void validateBoardAccessForGet(String boardId, String oidFromToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        if (board.getVisibility() == Board.Visibility.PUBLIC) {
            return;
        }

        if (!board.getOwnerId().equals(oidFromToken)) {
            User user = userRepository.findByOid(oidFromToken);
            if (user == null) {
                throw new ItemNotFoundException("User with id " + oidFromToken + " not found");
            }

            Collaborator collaborator = collaboratorRepository.findByUserOidAndBoardId(user.getOid(), boardId);
            if (collaborator == null ||
                    (collaborator.getAccessRight() != Collaborator.AccessRight.READ &&
                            collaborator.getAccessRight() != Collaborator.AccessRight.WRITE)) {
                throw new ForbiddenException("You do not have permission to access this board");
            }

        }
    }

    public void validateWriteAccess(String boardId, String oidFromToken) {
        // Find the board by its ID
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // Logging for debugging
        System.out.println("Owner ID: " + board.getOwnerId());
        System.out.println("OID from Token: " + oidFromToken);

        // Check if the user is the board owner
        if (!board.getOwnerId().equals(oidFromToken)) {
            throw new ForbiddenException("Only the board owner has permission to modify this board");
        }
    }




    public void validateDeleteAccess(String boardId, String oidFromToken) {
        // Retrieve the board using the provided boardId
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // Check if the user (oidFromToken) is the owner of the board
        if (!board.getOwnerId().equals(oidFromToken)) {
            // If the user is not the owner, check if they have WRITE access as a collaborator
            User user = userRepository.findByOid(oidFromToken);
            if (user == null) {
                throw new ItemNotFoundException("User with id " + oidFromToken + " not found");
            }

            // Find if the user is a collaborator with WRITE access
            Collaborator collaborator = collaboratorRepository.findByUserOidAndBoard(user.getOid(), board);
            if (collaborator == null || collaborator.getAccessRight() != Collaborator.AccessRight.WRITE) {
                // If not the owner or a collaborator with WRITE access, throw a ForbiddenException
                throw new ForbiddenException("You do not have permission to modify or delete this board");
            }
        }
    }


    public boolean hasPendingInvitation(String boardId, String email) {
        return invitationRepository.existsByBoardIdAndInviteeEmailAndStatus(boardId, email, Invitation.Status.PENDING);
    }




//    public Map<String, Object> getCollaboratorsByBoardId(String boardId, String oidFromToken) {
//        // ตรวจสอบสิทธิ์การเข้าถึงบอร์ด
//        validateBoardAccessForGet(boardId, oidFromToken);
//
//        // ดึงข้อมูลบอร์ดจากฐานข้อมูล Board
//        Board board = boardRepository.findById(boardId)
//                .orElseThrow(() -> new ItemNotFoundException("Board not found"));
//
//        // ดึง collaborators ที่เชื่อมโยงกับบอร์ดนี้จากฐานข้อมูล Collaborator
//        List<Collaborator> collaborators = collaboratorRepository.findByBoard(board);
//
//        // แปลง collaborators เป็น DTO
//        List<CollaboratorDTO> collaboratorDTOs = new ArrayList<>();
//        for (Collaborator collaborator : collaborators) {
//            // ดึงข้อมูล user โดยใช้ userOid จาก Collaborator
//            User user = userRepository.findByOid(collaborator.getUserOid());
//            if (user == null) {
//                throw new ItemNotFoundException("User not found for OID: " + collaborator.getUserOid());
//            }
//
//            // สร้าง CollaboratorDTO และใส่ข้อมูลที่ได้จาก User และ Collaborator
//            CollaboratorDTO collaboratorDTO = new CollaboratorDTO();
//            collaboratorDTO.setOid(user.getOid());;
//            collaboratorDTO.setName(user.getName());
//            collaboratorDTO.setEmail(user.getEmail());
//            collaboratorDTO.setAccessRight(collaborator.getAccessRight());
//            collaboratorDTO.setAddedOn(collaborator.getAddedOn().toString());  // แปลง ZonedDateTime เป็น String
//
//            collaboratorDTOs.add(collaboratorDTO);
//        }
//
//        // สร้างแผนที่สำหรับ response
//        Map<String, Object> response = new HashMap<>();
//        response.put("collaborators", collaboratorDTOs);
//
//        return response;
//    }


    public CollaboratorListDTO getCollaboratorsByBoardId(String boardId) {

        /// Fetch collaborators for the specified boardId
        List<Collaborator> collaborators = collaboratorRepository.findByBoardId(boardId);

        // Map collaborators to CollaboratorDTO
        List<CollaboratorDTO> collaboratorList = collaborators.stream()
                .map(collaborator -> {
                    // Fetch the name based on the user's OID
                    String name = userRepository.findNameByOid(collaborator.getUserOid());

                    return new CollaboratorDTO(
                            collaborator.getUserOid(),
                            name,  // Set the name fetched by OID
                            collaborator.getEmail(),
                            collaborator.getAccessRight(),
                            collaborator.getAddedOn().toString()
                    );
                })
                .collect(Collectors.toList());

        // Fetch pending invitations for the specific board
        List<InvitationDTO> pendingInvitations = invitationRepository.findByBoardIdAndStatus(
                        boardId, Invitation.Status.PENDING
                ).stream()
                .map(invitation -> new InvitationDTO(
                        invitation.getId(),
                        invitation.getInviterName(),
                        invitation.getInviteeName(),
                        invitation.getInviteeEmail(),
                        invitation.getBoard().getId(),
                        invitation.getBoard().getName(),
                        invitation.getAccessRight().name(),
                        invitation.getStatus().name(),
                        invitation.getCreatedOn().toString()
                ))
                .collect(Collectors.toList());

        // Map pending invitations to InvitationBoardDTO
        List<InvitationBoardDTO> invitationList = pendingInvitations.stream()
                .map(pendingInvitationDTO -> new InvitationBoardDTO(
                        pendingInvitationDTO.getInvitationId(),
                        pendingInvitationDTO.getInviterName(),
                        pendingInvitationDTO.getInviteeEmail(),
                        pendingInvitationDTO.getBoardId(),
                        pendingInvitationDTO.getBoardName(),
                        pendingInvitationDTO.getAccessRight(),
                        pendingInvitationDTO.getStatus()
                ))
                .collect(Collectors.toList());

        // Return CollaboratorListDTO with collaborators and pending invitations
        return new CollaboratorListDTO(collaboratorList, invitationList);
    }







    public CollaboratorDTO getCollaboratorById(String boardId, String collabOid, String oidFromToken) {
        validateBoardAccessForGet(boardId, oidFromToken);

        User user = userRepository.findById(collabOid)
                .orElseThrow(() -> new ItemNotFoundException("User with id " + collabOid + " not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        Collaborator collaborator = collaboratorRepository.findByUserOidAndBoard(user.getOid(), board);
        if (collaborator == null) {
            throw new ItemNotFoundException("Collaborator not found");
        }

        CollaboratorDTO collaboratorDTO = new CollaboratorDTO();
        collaboratorDTO.setOid(user.getOid());
        collaboratorDTO.setName(user.getName());
        collaboratorDTO.setEmail(user.getEmail());
        collaboratorDTO.setAccessRight(collaborator.getAccessRight());
        collaboratorDTO.setAddedOn(collaborator.getAddedOn().toString());

        return collaboratorDTO;
    }

    @Transactional
    public InvitationCDTO addCollaboratorByEmail(String boardId, String email, String accessRight, String oidFromToken, String inviteeName) {
        // Retrieve the inviter's name based on the OID from the token
        User inviter = userRepository.findByOid(oidFromToken);
        if (inviter == null) {
            throw new ItemNotFoundException("Inviter not found with OID: " + oidFromToken);
        }

        String inviterName = inviter.getName(); // Get the name from the User entity

        validateWriteAccess(boardId, oidFromToken);

        // Find board by ID
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        boolean isCollaborator = collaboratorRepository.existsByBoardIdAndUserOid(board.getId(), board.getOwnerId());
        boolean hasPendingInvitation = hasPendingInvitation(boardId, email);

        if (isCollaborator || hasPendingInvitation) {
            throw new ConflictException("User is already a collaborator or has a pending invitation for this board");
        }

        Collaborator.AccessRight accessRightEnum;
        try {
            accessRightEnum = Collaborator.AccessRight.valueOf(accessRight);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid access right provided. Valid values are READ or WRITE.");
        }

        // Create a new invitation with pending status
        Invitation invitation = new Invitation();
        invitation.setInviterName(inviterName); // Set the inviter's name here
        invitation.setInviteeName(inviteeName); // Set the invitee's name here
        invitation.setInviteeEmail(email);
        invitation.setBoard(board);
        invitation.setAccessRight(accessRightEnum);
        invitation.setStatus(Invitation.Status.PENDING);

        // Save the invitation in the database
        invitation = invitationRepository.save(invitation);

        // Send email confirmation for the invitation
        emailService.sendConfirmationEmail(email, board, invitation.getId(), inviteeName, accessRight); // Pass the invitee name here

        // Convert the invitation to InvitationDTO and return it
        InvitationCDTO invitationCDTO = new InvitationCDTO();
        invitationCDTO.setInviterName(invitation.getInviterName());
        invitationCDTO.setInviteeName(invitation.getInviteeName());
        invitationCDTO.setInviteeEmail(invitation.getInviteeEmail());
        invitationCDTO.setBoardName(board.getName());
        invitationCDTO.setAccessRight(invitation.getAccessRight().name());
        invitationCDTO.setStatus(invitation.getStatus().name());
        invitationCDTO.setCreatedOn(invitation.getCreatedOn().toString());

        return invitationCDTO;
    }







    public InvitationDTO getPendingInvitationForUser(String boardId, String oid) {
        // Fetch the user's name using the OID
        User user = userRepository.findByOid(oid);
        if (user == null) {
            throw new ItemNotFoundException("User not found with OID: " + oid);
        }

        String inviteeName = user.getName(); // Get the invitee's name from the user object

        // Fetch a single invitation where the invitee_name matches the user's name and board_id matches
        Invitation invitation = invitationRepository.findByBoardIdAndInviteeNameAndStatus(boardId, inviteeName, Invitation.Status.PENDING);

        if (invitation == null) {
            throw new ItemNotFoundException("No pending invitation found for user: " + inviteeName + " on board: " + boardId);
        }

        // Map the invitation to InvitationDTO
        InvitationDTO invitationDTO = new InvitationDTO();
        invitationDTO.setInvitationId(invitation.getId()); // Set the invitationId here
        invitationDTO.setInviterName(invitation.getInviterName());
        invitationDTO.setInviteeName(invitation.getInviteeName());
        invitationDTO.setInviteeEmail(invitation.getInviteeEmail());
        invitationDTO.setBoardId(invitation.getBoard().getId());
        invitationDTO.setBoardName(invitation.getBoard().getName());
        invitationDTO.setAccessRight(invitation.getAccessRight().name());
        invitationDTO.setStatus(invitation.getStatus().name());
        invitationDTO.setCreatedOn(invitation.getCreatedOn().toString());

        return invitationDTO;
    }




    @Transactional
    public void confirmCollaborator(Integer invitationId, boolean acceptInvitation, String oidFromToken) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ItemNotFoundException("Invitation not found"));

        if (invitation.getStatus() != Invitation.Status.PENDING) {
            throw new ConflictException("Invitation is not in a pending state");
        }

        // Handle accepting the invitation
        if (acceptInvitation) {
            invitation.setStatus(Invitation.Status.ACCEPTED);

            // Create a new collaborator from the accepted invitation
            Collaborator collaborator = new Collaborator();
            collaborator.setUserOid(oidFromToken);
            collaborator.setBoard(invitation.getBoard());
            collaborator.setEmail(invitation.getInviteeEmail());
            collaborator.setAccessRight(invitation.getAccessRight());
            collaborator.setAddedOn(ZonedDateTime.now());

            collaboratorRepository.save(collaborator); // Save the new collaborator to the database
        }
        // Handle declining the invitation
        else {
            invitation.setStatus(Invitation.Status.DECLINED);
        }

        invitationRepository.save(invitation); // Save the updated invitation
    }



    @Transactional
    public CollaboratorDTO updateCollaboratorAccessRight(String boardId, String collabOid, String newAccessRight, String oidFromToken) {
        validateWriteAccess(boardId, oidFromToken);

        User user = userRepository.findById(collabOid)
                .orElseThrow(() -> new ItemNotFoundException("User with id " + collabOid + " not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        Collaborator collaborator = collaboratorRepository.findByUserOidAndBoard(user.getOid(), board);
        if (collaborator == null) {
            throw new ItemNotFoundException("Collaborator not found");
        }

        // อัปเดต accessRight ใหม่
        collaborator.setAccessRight(Collaborator.AccessRight.valueOf(newAccessRight));
        collaboratorRepository.save(collaborator);

        CollaboratorDTO collaboratorDTO = listMapper.mapObject(collaborator, CollaboratorDTO.class);
        collaboratorDTO.setOid(user.getOid());
        collaboratorDTO.setName(user.getName());
        collaboratorDTO.setEmail(user.getEmail());

        return collaboratorDTO;
    }

    @Transactional
    public CollaboratorDTO removeCollaboratorFromBoard(String boardId, String collabOid, String oidFromToken) {
        // Check if the board exists
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // Retrieve the user (collaborator being removed) by their OID
        User user = userRepository.findByOid(collabOid);
        if (user == null) {
            // If the collaborator does not exist, throw 404 for non-existing collaborator case
            throw new ItemNotFoundException("Collaborator with id " + collabOid + " does not exist.");
        }

        // Check if the current user is either the board owner or a collaborator with sufficient access
        boolean isOwner = board.getOwnerId().equals(oidFromToken);
        Collaborator currentCollaborator = collaboratorRepository.findByUserOidAndBoard(oidFromToken, board);
        Collaborator collaboratorToRemove = collaboratorRepository.findByUserOidAndBoard(collabOid, board);

        // Case: non-collaborator cannot remove collaborator (including unauthenticated user)
        if (currentCollaborator == null && !isOwner) {
            throw new ForbiddenException("You do not have permission to remove collaborators from this board.");
        }

        // Case: collaborator does not exist on the board
        if (collaboratorToRemove == null) {
            // Owner cannot remove non-existing collaborator
            if (isOwner) {
                throw new ItemNotFoundException("Collaborator does not exist on this board.");
            }
            // Non-collaborator cannot leave if not part of the board
            throw new ForbiddenException("Collaborator does not exist or cannot leave this board.");
        }

        // Case: collaborator trying to remove themselves (READ or WRITE can remove themselves)
        if (oidFromToken.equals(collabOid)) {
            collaboratorRepository.delete(collaboratorToRemove);
        } else {
            // Case: READ or WRITE collaborators cannot remove other collaborators
            if (currentCollaborator != null && (currentCollaborator.getAccessRight() == Collaborator.AccessRight.READ ||
                    currentCollaborator.getAccessRight() == Collaborator.AccessRight.WRITE)) {
                throw new ForbiddenException("Collaborators cannot remove other collaborators.");
            }

            // Case: board owner can remove any collaborator
            if (isOwner) {
                collaboratorRepository.delete(collaboratorToRemove);
            } else {
                throw new ForbiddenException("Only the board owner can remove other collaborators.");
            }
        }

        // Map the collaborator entity to DTO before returning
        CollaboratorDTO collaboratorDTO = listMapper.mapObject(collaboratorToRemove, CollaboratorDTO.class);
        collaboratorDTO.setOid(user.getOid());
        collaboratorDTO.setName(user.getName());
        collaboratorDTO.setEmail(user.getEmail());

        return collaboratorDTO;
    }









}
