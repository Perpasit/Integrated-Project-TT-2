package sit.int221.integratedapi.services;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.repositories.primary.BoardRepository;

import java.util.List;
import java.util.Optional;
@NoArgsConstructor
@Qualifier("primaryDataSource")
@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    // Create a new board
    public Board createBoard(BoardDTO boardDTO) {
        Board board = new Board();
        return boardRepository.save(board);
    }

    // Get all boards
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    // Get a board by ID
    public Optional<Board> getBoardById(int id) {
        return boardRepository.findById(id);
    }

    // Delete a board by ID
    public void deleteBoardById(int id) {
        boardRepository.deleteById(id);
    }

    // Update a board
    public Board updateBoard(int id, BoardDTO boardDTO) {
        Optional<Board> existingBoard = boardRepository.findById(id);
        if (existingBoard.isPresent()) {
            Board board = existingBoard.get();
            board.setName(boardDTO.getName());
            board.setUsersOid(boardDTO.getUsersOid());
            return boardRepository.save(board);
        }
        return null;
    }
}
