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
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.exceptions.ValidationErrorException;
import sit.int221.integratedapi.services.BoardService;
import sit.int221.integratedapi.services.StatusService;
import sit.int221.integratedapi.services.TaskService;

import java.util.ArrayList;
import java.util.List;

import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.utils.ListMapper;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
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

//    @GetMapping("/{boardId}")
//    public ResponseEntity<BoardDTO> getBoardById(@RequestHeader("Authorization") String token, @PathVariable String boardId) {
//        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        BoardDTO boardDTO = boardService.getBoardById(boardId, oidFromToken);
//        return ResponseEntity.ok(boardDTO);
//    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDTO> getBoardById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId) {

        String oidFromToken = null;

        // Check if the token is provided
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                    claims -> claims.get("oid", String.class));
        }

        BoardDTO boardDTO = boardService.getBoardById(boardId, oidFromToken);
        return ResponseEntity.ok(boardDTO);
    }


    @PostMapping("")
    public ResponseEntity<?> createBoard(@Valid @RequestHeader("Authorization") String token,
                                         @RequestBody(required = false) BoardCDTO boardCDTO) {
        if (boardCDTO == null || boardCDTO.getName() == null) {
            return ResponseEntity.badRequest().build();
        }

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));
        BoardCDTO createdBoard = boardService.createBoard(boardCDTO, oidFromToken);
        return ResponseEntity.ok(createdBoard);
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@RequestHeader("Authorization") String token,
                                         @PathVariable String boardId,
                                         @RequestBody(required = false) BoardCDTO boardCDTO) {
        if (boardCDTO == null) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not exist!");
        }
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        BoardCDTO updatedBoard = boardService.updateBoard(boardId, boardCDTO, oidFromToken);
        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<BoardDTO> deleteBoard(@RequestHeader("Authorization") String token, @PathVariable String boardId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        BoardDTO deletedBoard = boardService.deleteBoard(boardId, oidFromToken);
        return ResponseEntity.ok(deletedBoard);
    }

    private boolean isValidSortBy(String sortBy) {
        return sortBy.equalsIgnoreCase("asc") || sortBy.equalsIgnoreCase("desc") || sortBy.equalsIgnoreCase("createOn");
    }

    @GetMapping("/{boardId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByBoardId(
            @RequestHeader(value = "Authorization", required = false) String token,  // ทำให้ optional
            @PathVariable String boardId,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) List<String> filterStatuses,
            @RequestParam(required = false) List<String> allParams) {

        String oidFromToken = null;

        // ตรวจสอบว่ามี token หรือไม่ (ในกรณีที่ผู้ใช้ล็อกอินมา)
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }

        taskService.validateBoardAccessForGet(boardId, oidFromToken);  // ตรวจสอบสิทธิ์การเป็นเจ้าของบอร์ด

        // ตรวจสอบ filter และ sort
        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        if (sortBy != null && !isValidSortBy(sortBy)) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        if (allParams != null && !allParams.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // เพิ่ม oidFromToken ลงในคำสั่งเรียกใช้ taskService.getAllTasks
        ResponseEntity<Object> response = taskService.getAllTasks(boardId, sortBy, filterStatuses, allParams);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Task> tasks = (List<Task>) response.getBody();
            List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
            return ResponseEntity.ok(taskDTOs);
        } else {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    @GetMapping("/{boardId}/tasks/{id}")
    public ResponseEntity<TaskRDTO> getTaskById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer id) {

        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }
        taskService.validateBoardAccessForGet(boardId, oidFromToken);
        TaskRDTO taskRDTO = taskService.getTaskDetailsById(boardId, id);
        return ResponseEntity.ok(taskRDTO);
    }


    @PostMapping("/{boardId}/tasks")
    public ResponseEntity<Object> addNewTask(
            @RequestHeader("Authorization") String token,
            @PathVariable String boardId,
            @Valid @RequestBody(required = false) TaskCDTO taskCDTO) {
        if (taskCDTO == null) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not exist!");
        }

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return taskService.createNewTask(taskCDTO, boardId);
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<Object> updateTask(
            @RequestHeader("Authorization") String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @Valid @RequestBody(required = false) TaskUDTO updatedTaskDTO) {
        if (updatedTaskDTO == null) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not exist!");
        }
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return taskService.updateTask(boardId, taskId, updatedTaskDTO);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<TaskDDTO> deleteTask(@RequestHeader("Authorization") String token,
                                               @PathVariable String boardId,
                                               @PathVariable Integer taskId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return taskService.removeTask(boardId, taskId);
    }

    @GetMapping("/{boardId}/statuses")
    public ResponseEntity<List<StatusDTO>> getStatuses(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }
        statusService.validateBoardAccessForGet(boardId, oidFromToken);
        List<StatusDTO> statusDTOs = statusService.getStatusesByBoardId(boardId);
        return ResponseEntity.ok(statusDTOs);
    }


    @GetMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<StatusRDTO> getStatusById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer statusId) {

        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }
        statusService.validateBoardAccessForGet(boardId, oidFromToken);
        StatusRDTO statusRDTO = statusService.getStatusByBoardIdAndStatusId(boardId, statusId);
        return ResponseEntity.ok(statusRDTO);
    }


    @PostMapping("/{boardId}/statuses")
    public ResponseEntity<Object> addNewStatus(@RequestHeader("Authorization") String token,
                                               @PathVariable String boardId,
                                               @Valid @RequestBody(required = false) StatusCDTO status) {
        if (status == null) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not exist!");
        }
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return statusService.createStatus(boardId, status);
    }

    @GetMapping("/{boardId}/statuses/{statusId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByBoardAndStatusId(
            @RequestHeader("Authorization") String token,
            @PathVariable String boardId,
            @PathVariable Integer statusId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        statusService.validateBoardAccessForGet(boardId,oidFromToken);
        List<TaskDTO> taskDTOs = statusService.getTasksByBoardAndStatusId(boardId, statusId);
        return ResponseEntity.ok(taskDTOs);
    }

    @PutMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> updateStatus(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody(required = false) StatusUDTO statusUDTO,
            @PathVariable String boardId,
            @PathVariable Integer statusId) {
        if (statusUDTO == null) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not exist!");
        }
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return statusService.updateStatus(boardId, statusId, statusUDTO);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<StatusDDTO> removeStatus(@RequestHeader("Authorization") String token,
                                                   @PathVariable String boardId,
                                                   @PathVariable Integer statusId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return statusService.removeStatus(boardId, statusId);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}/{newId}")
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(@RequestHeader("Authorization") String token,
                                                             @PathVariable String boardId,
                                                             @PathVariable Integer statusId,
                                                             @PathVariable Integer newId) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        return statusService.deleteTransferStatus(boardId, statusId, newId);
    }


}
