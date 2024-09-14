package sit.int221.integratedapi.services;

import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.dtos.OwnerDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.entities.User;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Board createBoard(BoardDTO boardDTO) {
        Board board = new Board();
        return boardRepository.save(board);
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
            // If taskUser is null, you can choose to either skip or handle the board differently
        }

        return boardDTOs;
    }



//    @Transactional
//    public List<BoardDTO> getBoardsByOid(String oid) {
//        // ตรวจสอบว่า oid ไม่เป็น null หรือว่างเปล่า
//        if (oid == null || oid.trim().isEmpty()) {
//            throw new IllegalArgumentException("OID must not be null or empty");
//        }
//
//        // ดึงข้อมูล Board จากฐานข้อมูลตาม OID
//        List<Board> boards = boardRepository.findBoardsByTaskusers_oid(oid);
//
//
//
//        // แปลงข้อมูล Board เป็น BoardDTO
//        return boards.stream()
//                .map(board -> modelMapper.map(board, BoardDTO.class))
//                .collect(Collectors.toList());
//    }


    // Delete a board by ID
    public void deleteBoardById(int id) {
        boardRepository.deleteById(id);
    }

    // Update a board
//    public Board updateBoard(int id, BoardDTO boardDTO) {
//        Optional<Board> existingBoard = boardRepository.findById(id);
//        if (existingBoard.isPresent()) {
//            Board board = existingBoard.get();
//            board.setName(boardDTO.getName());
//            board.setUsersOid(boardDTO.getUsersOid());
//            return boardRepository.save(board);
//        }
//        return null;
//    }
}
