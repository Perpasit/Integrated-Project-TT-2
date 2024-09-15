package sit.int221.integratedapi.services;

import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.BoardCDTO;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.dtos.OwnerDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.exceptions.ValidationErrorException;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
import sit.int221.integratedapi.utils.ListMapper;
import sit.int221.integratedapi.exceptions.BoardOwnershipException;


import java.util.ArrayList;
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



    public boolean existsById(Integer boardId) {
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
                boardDTO.setId(board.getId());
                boardDTO.setName(board.getName());
                boardDTO.setOwner(ownerDTO);

                boardDTOs.add(boardDTO);
            }
            // If taskUser is null, you can choose to either skip or handle the board differently
        }

        return boardDTOs;
    }

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
        boolean boardExists = doesBoardExistForUser(oid);
        if (boardExists) {
            throw new BoardOwnershipException("User can only own one board.");
        }

        // Throw validation errors if any
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        // Create and save new board
        Board board = new Board();
        board.setName(boardCDTO.getName());
        board.setTaskusersOid(oid);
        boardRepository.save(board);

        return new BoardCDTO(board.getId(), board.getName());
    }

    public boolean doesBoardExistForUser(String taskusersOid) {
        // Check if a board already exists for the given taskusers_oid
        return boardRepository.existsByTaskusersOid(taskusersOid);
    }

    public BoardCDTO updateBoard(Integer boardId, BoardCDTO boardCDTO, String oid) {
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

    public BoardDTO deleteBoard(Integer boardId, String oid) {

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
