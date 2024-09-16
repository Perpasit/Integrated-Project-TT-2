package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.services.BoardService;
import sit.int221.integratedapi.services.StatusService;
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
    private StatusService statusService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ListMapper listMapper;

    @GetMapping("")
    public ResponseEntity<List<BoardDTO>> getAllBoards(@RequestHeader("Authorization") String token) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        List<BoardDTO> boardDTOs = boardService.getAllBoards(oidFromToken);
        return ResponseEntity.ok(boardDTOs);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDTO> getBoardById(@RequestHeader("Authorization") String token, @PathVariable Integer boardId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        BoardDTO boardDTO = boardService.getBoardById(boardId, oidFromToken);
        return ResponseEntity.ok(boardDTO);
    }


    @PostMapping("")
    public ResponseEntity<?> createBoard(@Valid @RequestHeader("Authorization") String token, @RequestBody BoardCDTO boardCDTO) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        BoardCDTO createdBoard = boardService.createBoard(boardCDTO, oidFromToken);
        return ResponseEntity.ok(createdBoard);
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@RequestHeader("Authorization") String token,
                                         @PathVariable Integer boardId,
                                         @RequestBody BoardCDTO boardCDTO) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        BoardCDTO updatedBoard = boardService.updateBoard(boardId, boardCDTO, oidFromToken);
        return ResponseEntity.ok(updatedBoard);

    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<BoardDTO> deleteBoard(@RequestHeader("Authorization") String token, @PathVariable Integer boardId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        BoardDTO deletedBoard = boardService.deleteBoard(boardId, oidFromToken);
        return ResponseEntity.ok(deletedBoard);
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

    @GetMapping("/{boardId}/tasks/{id}")
    public ResponseEntity<TaskRDTO> getTaskById(@RequestHeader("Authorization") String token,
                                                @PathVariable Integer boardId,
                                                @PathVariable Integer id) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));

        TaskRDTO taskRDTO = taskService.getTaskDetailsById(boardId, id, oidFromToken);

        return ResponseEntity.ok(taskRDTO);
    }



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


    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<TaskDDTO> deleteTask(@PathVariable Integer boardId, @PathVariable Integer taskId) {
        return taskService.removeTask(boardId, taskId);
    }

    @GetMapping("/{boardId}/statuses")
    public ResponseEntity<List<StatusDTO>> getStatuses(@PathVariable Integer boardId) {
        List<StatusDTO> statusDTOs = statusService.getStatusesByBoardId(boardId);
        return ResponseEntity.ok(statusDTOs);
    }

    @GetMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<StatusRDTO> getStatusById(@PathVariable Integer boardId, @PathVariable Integer statusId) {
        StatusRDTO statusRDTO = statusService.getStatusByBoardIdAndStatusId(boardId, statusId);
        return ResponseEntity.ok(statusRDTO);
    }

    @PostMapping("/{boardId}/statuses")
    public ResponseEntity<Object> addNewStatus(@PathVariable Integer boardId, @Valid @RequestBody StatusCDTO status) {
        return statusService.createStatus(boardId, status);
    }

    @GetMapping("/{boardId}/statuses/{statusId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByBoardAndStatusId(
            @PathVariable Integer boardId,
            @PathVariable Integer statusId) {

        List<TaskDTO> taskDTOs = statusService.getTasksByBoardAndStatusId(boardId, statusId);
        return ResponseEntity.ok(taskDTOs);
    }

    @PutMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> updateStatus(
            @Valid @RequestBody StatusUDTO statusUDTO,
            @PathVariable Integer boardId,
            @PathVariable Integer statusId) {

        return statusService.updateStatus(boardId, statusId, statusUDTO);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<StatusDDTO> removeStatus(@PathVariable Integer boardId, @PathVariable Integer statusId) {
        return statusService.removeStatus(boardId, statusId);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}/{newId}")
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(@PathVariable Integer boardId,
                                                             @PathVariable Integer statusId,
                                                             @PathVariable Integer newId) {
        return statusService.deleteTransferStatus(boardId, statusId, newId);
    }
    
}
