package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.CollaboratorDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.exceptions.ConflictException;
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.login.UserRepository;
import sit.int221.integratedapi.repositories.primary.CollaboratorRepository;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CollaboratorService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListMapper listMapper;


    public boolean isUserAlreadyCollaborator(String boardId, String email) {
        return collaboratorRepository.existsByBoardIdAndEmail(boardId, email);
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

        // Only allow the owner of the board to modify it
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




    public Map<String, Object> getCollaboratorsByBoardId(String boardId, String oidFromToken) {
        // ตรวจสอบสิทธิ์การเข้าถึงบอร์ด
        validateBoardAccessForGet(boardId, oidFromToken);

        // ดึงข้อมูลบอร์ดจากฐานข้อมูล Board
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // ดึง collaborators ที่เชื่อมโยงกับบอร์ดนี้จากฐานข้อมูล Collaborator
        List<Collaborator> collaborators = collaboratorRepository.findByBoard(board);

        // แปลง collaborators เป็น DTO
        List<CollaboratorDTO> collaboratorDTOs = new ArrayList<>();
        for (Collaborator collaborator : collaborators) {
            // ดึงข้อมูล user โดยใช้ userOid จาก Collaborator
            User user = userRepository.findByOid(collaborator.getUserOid());
            if (user == null) {
                throw new ItemNotFoundException("User not found for OID: " + collaborator.getUserOid());
            }

            // สร้าง CollaboratorDTO และใส่ข้อมูลที่ได้จาก User และ Collaborator
            CollaboratorDTO collaboratorDTO = new CollaboratorDTO();
            collaboratorDTO.setOid(user.getOid());;
            collaboratorDTO.setName(user.getName());
            collaboratorDTO.setEmail(user.getEmail());
            collaboratorDTO.setAccessRight(collaborator.getAccessRight());
            collaboratorDTO.setAddedOn(collaborator.getAddedOn().toString());  // แปลง ZonedDateTime เป็น String

            collaboratorDTOs.add(collaboratorDTO);
        }

        // สร้างแผนที่สำหรับ response
        Map<String, Object> response = new HashMap<>();
        response.put("collaborators", collaboratorDTOs);

        return response;
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
    public CollaboratorDTO addCollaboratorByEmail(String boardId, String email, String accessRight, String oidFromToken) {
        // Validate if the current user has write access to the board
        validateWriteAccess(boardId, oidFromToken);

        // Find user by email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new ItemNotFoundException("User not found with email: " + email);
        }

        // Find board by ID
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        // Check if the user is already a collaborator on this board
        boolean exists = collaboratorRepository.existsByBoardIdAndEmail(board.getId(), user.getEmail());
        if (exists) {
            throw new ConflictException("User is already a collaborator for this board");
        }

        // Validate access right and catch if the value provided does not exist in the enum
        Collaborator.AccessRight accessRightEnum;
        try {
            accessRightEnum = Collaborator.AccessRight.valueOf(accessRight);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid access right provided. Valid values are READ or WRITE.");
        }

        // Create a new collaborator and set userOid, board, and email
        Collaborator newCollaborator = new Collaborator();
        newCollaborator.setUserOid(user.getOid());
        newCollaborator.setBoard(board);
        newCollaborator.setEmail(user.getEmail());
        newCollaborator.setAccessRight(accessRightEnum);

        // Save the new collaborator in the database
        newCollaborator = collaboratorRepository.save(newCollaborator);

        // Map the new collaborator to a DTO and return it
        CollaboratorDTO collaboratorDTO = new CollaboratorDTO();
        collaboratorDTO.setOid(user.getOid());
        collaboratorDTO.setName(user.getName());
        collaboratorDTO.setEmail(user.getEmail());
        collaboratorDTO.setAccessRight(newCollaborator.getAccessRight());
        collaboratorDTO.setAddedOn(newCollaborator.getAddedOn().toString());

        return collaboratorDTO;
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
