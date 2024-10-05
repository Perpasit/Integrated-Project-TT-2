package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.services.TaskService;
import sit.int221.integratedapi.utils.ListMapper;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class TaskController {
    private final TaskService taskService;
    private final ListMapper listMapper;

    private final JwtTokenUtil jwtTokenUtil;

    public TaskController(TaskService taskService, ListMapper listMapper,JwtTokenUtil jwtTokenUtil) {
        this.taskService = taskService;
        this.listMapper = listMapper;
        this.jwtTokenUtil = jwtTokenUtil;
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
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @Valid @RequestBody(required = false) TaskCDTO taskCDTO) {

        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        taskService.validateBoardOwnershipForModification(boardId, oidFromToken);
        if (taskCDTO == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }
        return taskService.createNewTask(taskCDTO, boardId);
    }

    @PutMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<Object> updateTask(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @Valid @RequestBody(required = false) TaskUDTO updatedTaskDTO) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        taskService.findTaskById(taskId);
        if (updatedTaskDTO == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }
        return taskService.updateTask(boardId, taskId, updatedTaskDTO);
    }

    @DeleteMapping("/{boardId}/tasks/{taskId}")
    public ResponseEntity<TaskDDTO> deleteTask(@RequestHeader(value = "Authorization", required = false) String token,
                                               @PathVariable String boardId,
                                               @PathVariable Integer taskId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        taskService.findTaskById(taskId);
        return taskService.removeTask(boardId, taskId);
    }

}