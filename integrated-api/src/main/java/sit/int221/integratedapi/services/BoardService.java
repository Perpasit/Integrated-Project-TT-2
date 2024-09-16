package sit.int221.integratedapi.services;

import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.BoardCDTO;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.dtos.OwnerDTO;
import sit.int221.integratedapi.dtos.StatusCDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.exceptions.ValidationErrorException;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.StatusRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
import sit.int221.integratedapi.utils.ListMapper;
import sit.int221.integratedapi.exceptions.BoardOwnershipException;
import sit.int221.integratedapi.utils.NanoIdGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private StatusService statusService;

    public boolean existsById(String boardId) {
        return boardRepository.existsById(boardId);
    }

    public List<BoardDTO> getAllBoards(String oid) {
        // Retrieve all boards where the task user's oid matches the given oid
        List<Board> boards = boardRepository.findByTaskusersOid(oid);
        List<BoardDTO> boardDTOs = new ArrayList<>();

        for (Board board : boards) {
            // Fetch TaskUser by oid directly (null check will be used instead of Optional)
            TaskUser taskUser = taskUserRepository.findByOid(board.getTaskusersOid());

            if (taskUser != null) {
                OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());

                // Create and populate BoardDTO
                BoardDTO boardDTO = new BoardDTO();
                boardDTO.setBoardId(board.getId());
                boardDTO.setName(board.getName());
                boardDTO.setOwner(ownerDTO);

                boardDTOs.add(boardDTO);
            }

        }

        return boardDTOs;
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
        } else if (boardCDTO.getName().length() > 45) {
            validationErrors.addError("name", "must not exceed 45 characters");
        }

        // Check for board ownership
//        boolean boardExists = doesBoardExistForUser(oid);
//        if (boardExists) {
//            throw new BoardOwnershipException("User can only own one board.");
//        }

        // Throw validation errors if any
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        Board board = new Board();
        board.setName(boardCDTO.getName());
        board.setTaskusersOid(oid);
        board.setId(NanoIdGenerator.generateNanoId(10)); // ใช้ NanoId generator
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

        // Return created board
        BoardCDTO createdBoardCDTO = new BoardCDTO(board.getId(), board.getName());
        return createdBoardCDTO;
    }

    public boolean doesBoardExistForUser(String taskusersOid) {
        // Check if a board already exists for the given taskusers_oid
        return boardRepository.existsByTaskusersOid(taskusersOid);
    }

    public BoardDTO getBoardById(String boardId, String oid) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with id: " + boardId));

        if (!board.getTaskusersOid().equals(oid)) {
            throw new BoardOwnershipException("User does not own this board.");
        }

        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);

        TaskUser taskUser = taskUserRepository.findById(board.getTaskusersOid())
                .orElseThrow(() -> new ItemNotFoundException("User not found with id: " + board.getTaskusersOid()));

        OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());
        boardDTO.setOwner(ownerDTO);

        return boardDTO;
    }


    @Transactional
    public BoardCDTO updateBoard(String boardId, BoardCDTO boardCDTO, String oid) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with id: " + boardId));

        if (!board.getTaskusersOid().equals(oid)) {
            throw new BoardOwnershipException("User does not own this board.");
        }

        // Update the board's name if it's valid
        if (boardCDTO.getName() != null && !boardCDTO.getName().isEmpty() && boardCDTO.getName().length() <= 120) {
            board.setName(boardCDTO.getName());
            boardRepository.save(board);
        } else {
            throw new IllegalArgumentException("Invalid board name.");
        }

        return new BoardCDTO(board.getId(), board.getName());
    }

    @Transactional
    public BoardDTO deleteBoard(String boardId, String oid) {

        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board not found with id: " + boardId));


        if (!board.getTaskusersOid().equals(oid)) {
            throw new BoardOwnershipException("User does not own this board.");
        }


        BoardDTO boardDTO = modelMapper.map(board, BoardDTO.class);


        TaskUser taskUser = taskUserRepository.findById(board.getTaskusersOid())
                .orElseThrow(() -> new ItemNotFoundException("User not found with id: " + board.getTaskusersOid()));

        OwnerDTO ownerDTO = new OwnerDTO(taskUser.getOid(), taskUser.getName());
        boardDTO.setOwner(ownerDTO);

        boardRepository.delete(board);

        return boardDTO;
    }
}
