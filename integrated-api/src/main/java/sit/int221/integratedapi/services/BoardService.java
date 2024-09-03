package sit.int221.integratedapi.services;

import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.utils.ListMapper;

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

    public Board createBoard(BoardDTO boardDTO) {
        Board board = new Board();
        return boardRepository.save(board);
    }

    public List<BoardDTO> getAllBoards() {
        List<Board> boards = boardRepository.findAll();
        return boards.stream()
                .map(board -> modelMapper.map(board, BoardDTO.class))
                .collect(Collectors.toList());
    }


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
