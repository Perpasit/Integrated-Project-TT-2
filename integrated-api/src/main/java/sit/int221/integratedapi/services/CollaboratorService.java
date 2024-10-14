package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.CollaboratorDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.primary.CollaboratorRepository;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
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
    private TaskUserRepository taskUserRepository;

    @Autowired
    private ListMapper listMapper;


    public boolean isUserAlreadyCollaborator(String boardId, String userEmail) {
        return collaboratorRepository.existsByBoardIdAndUserEmail(boardId, userEmail);
    }

    public boolean isBoardOwner(String boardId, String userId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));
        return board.getOwnerId().equals(userId);
    }

    public boolean isEmailOfBoardOwner(String boardId, String email) {
        // Get the user associated with the email
        TaskUser user = taskUserRepository.findByEmail(email)
                .orElseThrow(() -> new ItemNotFoundException("User with email " + email + " not found"));

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
            TaskUser user = taskUserRepository.findByOid(oidFromToken);
            if (user == null) {
                throw new ItemNotFoundException("User with id " + oidFromToken + " not found");
            }

            Collaborator collaborator = collaboratorRepository.findByUserAndBoard(user, board);
            if (collaborator == null ||
                    (collaborator.getAccessRight() != Collaborator.AccessRight.READ &&
                            collaborator.getAccessRight() != Collaborator.AccessRight.WRITE)) {
                throw new ForbiddenException("You do not have permission to access this board");
            }
        }
    }

    public void validateWriteAccess(String boardId, String oidFromToken) {
        // ค้นหาบอร์ดที่ต้องการเข้าถึง
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // ตรวจสอบว่า oidFromToken เป็นเจ้าของบอร์ดหรือเป็น collaborator ที่มีสิทธิ์ WRITE
        if (!board.getOwnerId().equals(oidFromToken)) {
            TaskUser user = taskUserRepository.findByOid(oidFromToken);
            if (user == null) {
                throw new ItemNotFoundException("User with id " + oidFromToken + " not found");
            }

            Collaborator collaborator = collaboratorRepository.findByUserAndBoard(user, board);
            if (collaborator == null || collaborator.getAccessRight() != Collaborator.AccessRight.WRITE) {
                throw new ForbiddenException("You do not have permission to modify this board");
            }
        }
    }

    public Map<String, Object> getCollaboratorsByBoardId(String boardId, String oidFromToken) {
        validateBoardAccessForGet(boardId, oidFromToken);

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        List<Collaborator> collaborators = collaboratorRepository.findByBoard(board);

        List<CollaboratorDTO> collaboratorDTOs = new ArrayList<>();

        for (Collaborator collaborator : collaborators) {
            TaskUser user = collaborator.getUser();
            CollaboratorDTO collaboratorDTO = new CollaboratorDTO();
            collaboratorDTO.setOid(user.getOid());
            collaboratorDTO.setName(user.getName());
            collaboratorDTO.setEmail(user.getEmail());
            collaboratorDTO.setAccessRight(collaborator.getAccessRight());
            collaboratorDTO.setAddedOn(collaborator.getAddedOn().toString());
            collaboratorDTOs.add(collaboratorDTO);
        }

        // Return as a map (object) instead of a list
        Map<String, Object> response = new HashMap<>();
        response.put("collaborators", collaboratorDTOs);
        return response;
    }

    public CollaboratorDTO getCollaboratorById(String boardId, String collabOid, String oidFromToken) {
        validateBoardAccessForGet(boardId, oidFromToken);

        TaskUser user = taskUserRepository.findById(collabOid)
                .orElseThrow(() -> new ItemNotFoundException("User with id " + collabOid + " not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        Collaborator collaborator = collaboratorRepository.findByUserAndBoard(user, board);
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
    public CollaboratorDTO addCollaboratorByEmail(String boardId, String userEmail, String accessRight, String oidFromToken) {
        validateWriteAccess(boardId, oidFromToken);

        TaskUser user = taskUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ItemNotFoundException("User with email " + userEmail + " not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        boolean exists = collaboratorRepository.existsByUserAndBoard(user, board);
        if (exists) {
            throw new IllegalArgumentException("User is already a collaborator for this board");
        }

        if (accessRight == null || accessRight.isEmpty()) {
            accessRight = "READ";
        }

        Collaborator newCollaborator = new Collaborator();
        newCollaborator.setUser(user);
        newCollaborator.setBoard(board);
        newCollaborator.setAccessRight(Collaborator.AccessRight.valueOf(accessRight));

        newCollaborator = collaboratorRepository.save(newCollaborator);

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

        TaskUser user = taskUserRepository.findById(collabOid)
                .orElseThrow(() -> new ItemNotFoundException("User with id " + collabOid + " not found"));

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        Collaborator collaborator = collaboratorRepository.findByUserAndBoard(user, board);
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
        // ดึงบอร์ดและผู้ใช้งานจาก repository
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found"));

        TaskUser user = taskUserRepository.findById(collabOid)
                .orElseThrow(() -> new ItemNotFoundException("User with id " + collabOid + " not found"));

        // ตรวจสอบว่าผู้ใช้ปัจจุบันเป็นเจ้าของบอร์ดหรือไม่ หรือเป็นผู้ที่ต้องการลบตนเอง
        if (!oidFromToken.equals(collabOid)) {
            // ถ้าไม่ใช่เจ้าของบอร์ดและไม่ตรงกับ collabOid ให้ตรวจสอบสิทธิ์
            validateWriteAccess(boardId, oidFromToken);
        }

        // ดึง collaborator และตรวจสอบว่ามีอยู่จริง
        Collaborator collaborator = collaboratorRepository.findByUserAndBoard(user, board);
        if (collaborator == null) {
            throw new ItemNotFoundException("Collaborator not found");
        }

        // ลบ collaborator และสร้าง DTO เพื่อนำกลับไปใช้
        CollaboratorDTO collaboratorDTO = listMapper.mapObject(collaborator, CollaboratorDTO.class);
        collaboratorDTO.setOid(user.getOid());
        collaboratorDTO.setName(user.getName());
        collaboratorDTO.setEmail(user.getEmail());

        collaboratorRepository.delete(collaborator);

        return collaboratorDTO;
    }


}
