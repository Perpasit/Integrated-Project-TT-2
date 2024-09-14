package sit.int221.integratedapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.dtos.TaskRDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.services.BoardService;
import sit.int221.integratedapi.services.TaskService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sit.int221.integratedapi.services.JwtTokenUtil;

@RestController
@RequestMapping("/v3/boards")
public class BoardController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody BoardDTO boardDTO) {
        Board newBoard = boardService.createBoard(boardDTO);
        return ResponseEntity.ok(newBoard);
    }

    @GetMapping("")
    public ResponseEntity<List<BoardDTO>> getAllBoards(@RequestHeader("Authorization") String token) {
        // Extract the OID from the token
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));

        // Retrieve boards based on the OID
        List<BoardDTO> boardDTOs = boardService.getAllBoards(oidFromToken);

        return ResponseEntity.ok(boardDTOs);
    }



//    @GetMapping("/{id}")
//    public ResponseEntity<Board> getBoardById(@PathVariable int id) {
//        Optional<Board> board = boardService.getBoardById(id);
//        return board.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
//    }

//    @PutMapping("/{id}")
//    public ResponseEntity<Board> updateBoard(@PathVariable int id, @RequestBody BoardDTO boardDTO) {
//        Board updatedBoard = boardService.updateBoard(id, boardDTO);
//        if (updatedBoard != null) {
//            return ResponseEntity.ok(updatedBoard);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        boardService.deleteBoardById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/{boardId}/tasks")
    public ResponseEntity<List<TaskRDTO>> getTasksByBoardNo(
            @PathVariable Integer boardId,
            @RequestHeader("Authorization") String token) {

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));

        if (!taskService.isBoardOwnedByOid(boardId, oidFromToken)) {
            return ResponseEntity.ok(new ArrayList<>());
//            return ResponseEntity.ok().build();
        }


        List<TaskRDTO> tasks = taskService.getTasksByBoardId(boardId);
        if (tasks.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(tasks);
    }




}
