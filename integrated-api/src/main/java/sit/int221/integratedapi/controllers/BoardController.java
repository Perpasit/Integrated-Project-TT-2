package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.BoardDTO;
import sit.int221.integratedapi.dtos.TaskCDTO;
import sit.int221.integratedapi.dtos.TaskDTO;
import sit.int221.integratedapi.dtos.TaskUDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.services.BoardService;
import sit.int221.integratedapi.services.TaskService;

import java.util.ArrayList;
import java.util.List;

import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.utils.ListMapper;

@RestController
@RequestMapping("/v3/boards")
public class BoardController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ListMapper listMapper;

    @PostMapping("/{boardId}/tasks")
    public ResponseEntity<Object> addNewTask(
            @PathVariable Integer boardId,
            @Valid @RequestBody TaskCDTO taskCDTO) {
        taskCDTO.setBoardId(boardId);
        return taskService.createNewTask(taskCDTO);
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<Object> updateTask(
            @PathVariable Integer boardId,
            @PathVariable Integer taskId,
            @Valid @RequestBody TaskUDTO updatedTaskDTO) {
        return taskService.updateTask(boardId, taskId, updatedTaskDTO);
    }

    @GetMapping("")
    public ResponseEntity<List<BoardDTO>> getAllBoards(@RequestHeader("Authorization") String token) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        List<BoardDTO> boardDTOs = boardService.getAllBoards(oidFromToken);
        return ResponseEntity.ok(boardDTOs);
    }


    @GetMapping("/{boardId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByBoardId(
            @PathVariable Integer boardId,
            @RequestHeader("Authorization") String token,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) List<String> filterStatuses,
            @RequestParam(required = false) List<String> allParams) {

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));

        if (!taskService.isBoardOwnedByOid(boardId, oidFromToken)) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        if (sortBy != null && !isValidSortBy(sortBy)) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        if (allParams != null && !allParams.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        ResponseEntity<Object> response = taskService.getAllTasks(sortBy, filterStatuses, allParams);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Task> tasks = (List<Task>) response.getBody();
            List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
            return ResponseEntity.ok(taskDTOs);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean isValidSortBy(String sortBy) {
        return sortBy.equalsIgnoreCase("asc") || sortBy.equalsIgnoreCase("desc") || sortBy.equalsIgnoreCase("createOn");
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable int id) {
        boardService.deleteBoardById(id);
        return ResponseEntity.noContent().build();
    }




}
