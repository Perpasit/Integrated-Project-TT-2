package sit.int221.integratedapi.services;

import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.exceptions.ValidationErrorException;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.CollaboratorRepository;
import sit.int221.integratedapi.repositories.primary.StatusRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
import sit.int221.integratedapi.utils.ListMapper;
import sit.int221.integratedapi.exceptions.BoardOwnershipException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.utils.NanoIdGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Qualifier("primaryDataSource")
@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ListMapper listMapper;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private TaskUserRepository taskUserRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private StatusService statusService;

    public boolean existsById(String boardId) {
        return boardRepository.existsById(boardId);
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

    public void validateBoardOwnershipForModification(String boardId, String oidFromToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        if (oidFromToken == null || !board.getOwnerId().equals(oidFromToken)) {
            throw new ForbiddenException("You do not have permission to modify this board.");
        }
    }

    //    public List<BoardDTO> getAllBoards(String oid) {
//        // Retrieve all boards where the task user's oid matches the given oid
//        List<Board> boards = boardRepository.findByOwnerId(oid);
//        List<BoardDTO> boardDTOs = new ArrayList<>();
//
//        for (Board board : boards) {
//            // Fetch TaskUser by oid directly (null check will be used instead of Optional)
//            TaskUser taskUser = taskUserRepository.findByOid(board.getOwnerId());
//
//            if (taskUser != null) {
//                OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());
//
//                // Create and populate BoardDTO
//                BoardDTO boardDTO = new BoardDTO();
//                boardDTO.setId(board.getId());
//                boardDTO.setName(board.getName());
//                boardDTO.setOwner(ownerDTO);
//
//                // Map the visibility field
//                boardDTO.setVisibility(board.getVisibility());
//
//                boardDTOs.add(boardDTO);
//            }
//        }
//
//        return boardDTOs;
//    }

    // ฟังก์ชันสำหรับแปลง Collaborator เป็น CollaboratorDTO
    private CollaboratorDTO mapCollaboratorToDTO(Collaborator collaborator) {
        TaskUser user = collaborator.getUser();
        if (user != null) {
            return new CollaboratorDTO(
                    user.getOid(),
                    user.getName(),
                    user.getEmail(),
                    collaborator.getAccessRight(),
                    collaborator.getAddedOn().toString()
            );
        } else {
            throw new ItemNotFoundException("User not found for collaborator with ID: " + collaborator.getId());
        }
    }

    // ฟังก์ชันหลักสำหรับดึงบอร์ดส่วนตัวและบอร์ดที่เป็น collaborator
    // ฟังก์ชันสำหรับแปลง Board เป็น BoardDTO
    private BoardDTO mapBoardToDTO(Board board) {
        TaskUser taskUser = taskUserRepository.findByOid(board.getOwnerId());
        if (taskUser != null) {
            OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());
            return new BoardDTO(board.getId(), board.getName(), board.getVisibility(), ownerDTO);
        } else {
            throw new ItemNotFoundException("Owner not found for board with ID: " + board.getId());
        }
    }

    // ฟังก์ชันหลักสำหรับดึงบอร์ดส่วนตัวและบอร์ดที่เป็น collaborator
    public BoardListDTO getAllPersonalAndCollaboratingBoards(String userOid) {
        // ดึงบอร์ดส่วนตัว
        List<Board> personalBoards = boardRepository.findByOwnerId(userOid);
        List<Collaborator> collaborators = collaboratorRepository.findByUserOid(userOid);

        // แปลงเป็น BoardDTO สำหรับ personalBoards
        List<BoardDTO> personalBoardDTOs = personalBoards.stream()
                .map(this::mapBoardToDTO)
                .collect(Collectors.toList());

        // แปลงบอร์ดที่เป็น collaborator เป็น BoardDTO
        List<BoardDTO> collabBoardDTOs = collaborators.stream()
                .map(collaborator -> {
                    Board board = collaborator.getBoard();
                    TaskUser taskUser = collaborator.getUser();
                    OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());

                    // คืนค่าเป็น BoardDTO โดยไม่ใช้ accessRight
                    return new BoardDTO(
                            board.getId(),
                            board.getName(),
                            board.getVisibility(),
                            ownerDTO
                    );
                })
                .collect(Collectors.toList());

        // คืนค่าในรูปแบบของ BoardListDTO
        return new BoardListDTO(personalBoardDTOs, collabBoardDTOs);
    }



    @Transactional
    public BoardCDTO createBoard(BoardCDTO boardCDTO, String oid) {

        // Initialize ValidationErrorException
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        // Validate BoardCDTO fields
        if (boardCDTO.getName() == null) {
            validationErrors.addError("name", "must not be null");
        } else if (boardCDTO.getName().isEmpty()) {
            validationErrors.addError("name", "must not be empty");
        } else if (boardCDTO.getName().length() > 120) {
            validationErrors.addError("name", "must not exceed 120 characters");
        }

        // ตรวจสอบและตั้งค่า visibility
        if (boardCDTO.getVisibility() == null) {
            boardCDTO.setVisibility(Board.Visibility.valueOf("PRIVATE")); // ใช้ค่า default เป็น PRIVATE
        }

        // Throw validation errors if any
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        Board board = new Board();
        board.setName(boardCDTO.getName());
        board.setOwnerId(oid);
        board.setVisibility(boardCDTO.getVisibility()); // Set the isPublic field
        board.setId(NanoIdGenerator.generateNanoId(10)); // Use NanoId generator
        board = boardRepository.save(board);

        // Define status data with boardId
        List<StatusCDTO> defaultStatuses = Arrays.asList(
                new StatusCDTO(null, "No Status", "The default status", board.getId(), "#C0C0C0"),
                new StatusCDTO(null, "To Do", "The task is included in the project", board.getId(), "#6D5ACF"),
                new StatusCDTO(null, "Doing", "Being worked on.", board.getId(), "#FFDF00"),
                new StatusCDTO(null, "Done", "Finished", board.getId(), "#00FF7F")
        );

        // Create statuses using StatusService
        for (StatusCDTO statusCDTO : defaultStatuses) {
            statusService.createStatus(board.getId(), statusCDTO);
        }

        String ownerId = board.getOwnerId(); // Store ownerId in a final variable
        TaskUser taskUser = taskUserRepository.findById(ownerId)
                .orElseThrow(() -> new ItemNotFoundException("User not found with id: " + ownerId));

        OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());

        // Create and return BoardCDTO
        BoardCDTO createdBoardCDTO = new BoardCDTO(board.getId(), board.getName(), board.getVisibility(), ownerDTO);
        return createdBoardCDTO;
    }


    public boolean doesBoardExistForUser(String taskusersOid) {
        // Check if a board already exists for the given taskusers_oid
        return boardRepository.existsByOwnerId(taskusersOid);
    }


    public BoardDTO getBoardById(String boardId, String oid) {
        // Validate board access using the new method
        validateBoardAccessForGet(boardId, oid);

        // Find the board by its ID (access check passed)
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with id: " + boardId));

        // Map the board to BoardDTO
        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);

        // Find the owner of the board
        TaskUser taskUser = taskUserRepository.findById(board.getOwnerId())
                .orElseThrow(() -> new ItemNotFoundException("User not found with id: " + board.getOwnerId()));

        // Create OwnerDTO
        OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());
        boardDTO.setOwner(ownerDTO);

        return boardDTO;
    }


    @Transactional
    public BoardCDTO updateBoard(String boardId, BoardCDTO boardCDTO, String oid) {
        // Fetch the board by ID
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with id: " + boardId));

        // Check ownership
        if (!board.getOwnerId().equals(oid)) {
            throw new ForbiddenException("User does not own this board.");
        }

        // Validate and update the board's name
        if (boardCDTO.getName() != null) {
            if (boardCDTO.getName().isEmpty() || boardCDTO.getName().length() > 120) {
                throw new IllegalArgumentException("Board name must not be empty and must not exceed 120 characters.");
            }
            board.setName(boardCDTO.getName());
        }


        if (boardCDTO.getVisibility() == null) {
            throw new IllegalArgumentException("Request body must contain a valid 'visibility' value.");
        }


        // ตรวจสอบและตั้งค่า visibility
        if (boardCDTO.getVisibility() != null) {
            try {
                Board.Visibility visibility = boardCDTO.getVisibility();
                board.setVisibility(visibility);
            } catch (IllegalArgumentException e) {
                throw new HttpMessageNotReadableException("Invalid visibility value. Must be either PUBLIC or PRIVATE.");
            }
        }


        board = boardRepository.save(board);


        String ownerId = board.getOwnerId();
        TaskUser taskUser = taskUserRepository.findById(ownerId)
                .orElseThrow(() -> new ItemNotFoundException("User not found with id: " + ownerId));

        OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());


        return new BoardCDTO(board.getId(), board.getName(), board.getVisibility(), ownerDTO);
    }


    @Transactional
    public BoardDTO deleteBoard(String boardId, String oid) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with id: " + boardId));


        if (!board.getOwnerId().equals(oid)) {
            throw new BoardOwnershipException("User does not own this board.");
        }


        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);


        TaskUser taskUser = taskUserRepository.findById(board.getOwnerId())
                .orElseThrow(() -> new ItemNotFoundException("User not found with id: " + board.getOwnerId()));

        OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());
        boardDTO.setOwner(ownerDTO);

        boardRepository.delete(board);

        return boardDTO;
    }


}
