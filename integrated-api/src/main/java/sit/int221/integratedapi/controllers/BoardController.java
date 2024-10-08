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
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.ForbiddenException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.exceptions.ValidationErrorException;
import sit.int221.integratedapi.services.BoardService;
import sit.int221.integratedapi.services.StatusService;
import sit.int221.integratedapi.services.TaskService;
import sit.int221.integratedapi.services.CollaboratorService;

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
    private CollaboratorService collaboratorService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ListMapper listMapper;

//    @GetMapping("")
//    public ResponseEntity<List<BoardDTO>> getAllBoards(@RequestHeader("Authorization") String token) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
//                    claims -> claims.get("oid", String.class));
//        }
//        List<BoardDTO> boardDTOs = boardService.getAllBoards(oidFromToken);
//        return ResponseEntity.ok(boardDTOs);
//    }

    @GetMapping("")
    public ResponseEntity<BoardListDTO> getAllBoards(@RequestHeader("Authorization") String token) {
        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));
        BoardListDTO boardListDTO = boardService.getAllPersonalAndCollaboratingBoards(oidFromToken);
        return ResponseEntity.ok(boardListDTO);
    }


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
        boardService.validateBoardAccessForGet(boardId, oidFromToken);
        BoardDTO boardDTO = boardService.getBoardById(boardId, oidFromToken);
        return ResponseEntity.ok(boardDTO);
    }


    @PostMapping("")
    public ResponseEntity<?> createBoard(@Valid @RequestHeader("Authorization") String token,
                                         @RequestBody(required = false) BoardCDTO boardCDTO) {
        if (boardCDTO == null || boardCDTO.getName() == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));
        BoardCDTO createdBoard = boardService.createBoard(boardCDTO, oidFromToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBoard);
    }

    @PatchMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@RequestHeader(value = "Authorization",required = false) String token,
                                         @PathVariable String boardId,
                                         @RequestBody(required = false) BoardCDTO boardCDTO) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                    claims -> claims.get("oid", String.class));
        }else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        boardService.validateBoardOwnershipForModification(boardId, oidFromToken);
        if (boardCDTO == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }
        BoardCDTO updatedBoard = boardService.updateBoard(boardId, boardCDTO, oidFromToken);


        return ResponseEntity.ok(updatedBoard);
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<BoardDTO> deleteBoard(@RequestHeader(value = "Authorization",required = false) String token, @PathVariable String boardId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                    claims -> claims.get("oid", String.class));
        }else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        boardService.validateBoardOwnershipForModification(boardId, oidFromToken);
        BoardDTO deletedBoard = boardService.deleteBoard(boardId, oidFromToken);
        return ResponseEntity.ok(deletedBoard);
    }

//    private boolean isValidSortBy(String sortBy) {
//        return sortBy.equalsIgnoreCase("asc") || sortBy.equalsIgnoreCase("desc") || sortBy.equalsIgnoreCase("createOn");
//    }
//
//    @GetMapping("/{boardId}/tasks")
//    public ResponseEntity<List<TaskDTO>> getTasksByBoardId(
//            @RequestHeader(value = "Authorization", required = false) String token,  // ทำให้ optional
//            @PathVariable String boardId,
//            @RequestParam(required = false) String sortBy,
//            @RequestParam(required = false) List<String> filterStatuses,
//            @RequestParam(required = false) List<String> allParams) {
//
//        String oidFromToken = null;
//
//        // ตรวจสอบว่ามี token หรือไม่ (ในกรณีที่ผู้ใช้ล็อกอินมา)
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//
//        taskService.validateBoardAccessForGet(boardId, oidFromToken);  // ตรวจสอบสิทธิ์การเป็นเจ้าของบอร์ด
//
//        // ตรวจสอบ filter และ sort
//        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
//            return ResponseEntity.ok(new ArrayList<>());
//        }
//
//        if (sortBy != null && !isValidSortBy(sortBy)) {
//            return ResponseEntity.ok(new ArrayList<>());
//        }
//
//        if (allParams != null && !allParams.isEmpty()) {
//            return ResponseEntity.ok(new ArrayList<>());
//        }
//
//        // เพิ่ม oidFromToken ลงในคำสั่งเรียกใช้ taskService.getAllTasks
//        ResponseEntity<Object> response = taskService.getAllTasks(boardId, sortBy, filterStatuses, allParams);
//        if (response.getStatusCode().is2xxSuccessful()) {
//            List<Task> tasks = (List<Task>) response.getBody();
//            List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
//            return ResponseEntity.ok(taskDTOs);
//        } else {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//
//
//
//    @GetMapping("/{boardId}/tasks/{id}")
//    public ResponseEntity<TaskRDTO> getTaskById(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId,
//            @PathVariable Integer id) {
//
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//        taskService.validateBoardAccessForGet(boardId, oidFromToken);
//        TaskRDTO taskRDTO = taskService.getTaskDetailsById(boardId, id);
//        return ResponseEntity.ok(taskRDTO);
//    }
//
//
//    @PostMapping("/{boardId}/tasks")
//    public ResponseEntity<Object> addNewTask(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId,
//            @Valid @RequestBody(required = false) TaskCDTO taskCDTO) {
//
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        taskService.validateBoardOwnershipForModification(boardId, oidFromToken);
//        if (taskCDTO == null) {
//            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
//        }
//        return taskService.createNewTask(taskCDTO, boardId);
//    }
//
//    @PutMapping("/{boardId}/tasks/{taskId}")
//    public ResponseEntity<Object> updateTask(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId,
//            @PathVariable Integer taskId,
//            @Valid @RequestBody(required = false) TaskUDTO updatedTaskDTO) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
//        taskService.findTaskById(taskId);
//        if (updatedTaskDTO == null) {
//            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
//        }
//        return taskService.updateTask(boardId, taskId, updatedTaskDTO);
//    }
//
//    @DeleteMapping("/{boardId}/tasks/{taskId}")
//    public ResponseEntity<TaskDDTO> deleteTask(@RequestHeader(value = "Authorization", required = false) String token,
//                                               @PathVariable String boardId,
//                                               @PathVariable Integer taskId) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
//        taskService.findTaskById(taskId);
//        return taskService.removeTask(boardId, taskId);
//    }

//    @GetMapping("/{boardId}/statuses")
//    public ResponseEntity<List<StatusDTO>> getStatuses(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//        statusService.validateBoardAccessForGet(boardId, oidFromToken);
//        List<StatusDTO> statusDTOs = statusService.getStatusesByBoardId(boardId);
//        return ResponseEntity.ok(statusDTOs);
//    }
//
//
//    @GetMapping("/{boardId}/statuses/{statusId}")
//    public ResponseEntity<StatusRDTO> getStatusById(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId,
//            @PathVariable Integer statusId) {
//
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//        statusService.validateBoardAccessForGet(boardId, oidFromToken);
//        StatusRDTO statusRDTO = statusService.getStatusByBoardIdAndStatusId(boardId, statusId);
//        return ResponseEntity.ok(statusRDTO);
//    }
//
//
//    @PostMapping("/{boardId}/statuses")
//    public ResponseEntity<Object> addNewStatus(@RequestHeader(value = "Authorization",required = false) String token,
//                                               @PathVariable String boardId,
//                                               @Valid @RequestBody(required = false) StatusCDTO status) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
//        if (status == null) {
//            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
//        }
//        return statusService.createStatus(boardId, status);
//    }
//
//    @GetMapping("/{boardId}/statuses/{statusId}/tasks")
//    public ResponseEntity<List<TaskDTO>> getTasksByBoardAndStatusId(
//            @RequestHeader(value = "Authorization",required = false) String token,
//            @PathVariable String boardId,
//            @PathVariable Integer statusId) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//        statusService.validateBoardAccessForGet(boardId,oidFromToken);
//        List<TaskDTO> taskDTOs = statusService.getTasksByBoardAndStatusId(boardId, statusId);
//        return ResponseEntity.ok(taskDTOs);
//    }
//
//    @PutMapping("/{boardId}/statuses/{statusId}")
//    public ResponseEntity<Object> updateStatus(
//            @RequestHeader(value = "Authorization",required = false) String token,
//            @Valid @RequestBody(required = false) StatusUDTO statusUDTO,
//            @PathVariable String boardId,
//            @PathVariable Integer statusId) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
//        statusService.findStatusByIdAndBoardId(statusId,boardId);
//        if (statusUDTO == null) {
//            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
//        }
//        return statusService.updateStatus(boardId, statusId, statusUDTO);
//    }
//
//    @DeleteMapping("/{boardId}/statuses/{statusId}")
//    public ResponseEntity<StatusDDTO> removeStatus(@RequestHeader(value = "Authorization",required = false) String token,
//                                                   @PathVariable String boardId,
//                                                   @PathVariable Integer statusId) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
//        statusService.findStatusByIdAndBoardId(statusId,boardId);
//        return statusService.removeStatus(boardId, statusId);
//    }
//
//    @DeleteMapping("/{boardId}/statuses/{statusId}/{newId}")
//    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(@RequestHeader(value = "Authorization",required = false) String token,
//                                                             @PathVariable String boardId,
//                                                             @PathVariable Integer statusId,
//                                                             @PathVariable Integer newId) {
//        String oidFromToken = null;
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        } else {
//            throw new CustomAuthenticationException("You do not have permission to access this resource.");
//        }
//        statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
//        statusService.findStatusByIdAndBoardId(statusId,boardId);
//        return statusService.deleteTransferStatus(boardId, statusId, newId);
//    }

//    @GetMapping("/{boardId}/collabs")
//    public ResponseEntity<List<CollaboratorDTO>> getCollaboratorsByBoardId(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId) {
//
//        String oidFromToken = null;
//
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//
//        List<CollaboratorDTO> collaborators = collaboratorService.getCollaboratorsByBoardId(boardId, oidFromToken);
//        return ResponseEntity.ok(collaborators);
//    }
//
//
//    @GetMapping("/{boardId}/collabs/{collabOid}")
//    public ResponseEntity<CollaboratorDTO> getCollaboratorById(
//            @RequestHeader(value = "Authorization", required = false) String token,
//            @PathVariable String boardId,
//            @PathVariable String collabOid) {
//
//        String oidFromToken = null;
//
//        if (token != null && !token.isEmpty()) {
//            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
//        }
//
//        boardService.validateBoardAccessForGet(boardId, oidFromToken);
//
//        CollaboratorDTO collaborator = collaboratorService.getCollaboratorById(boardId, collabOid, oidFromToken);
//        if (collaborator == null) {
//            throw new ItemNotFoundException("Collaborator not found");
//        }
//
//        return ResponseEntity.ok(collaborator);
//    }
//
//    @PostMapping("/{boardId}/collabs")
//    public ResponseEntity<CollaboratorDTO> addCollaborator(
//            @RequestHeader(value = "Authorization") String token,
//            @PathVariable String boardId,
//            @Valid @RequestBody CollaboratorCDTO collaboratorCDTO) {
//
//        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
//                claims -> claims.get("oid", String.class));
//
//        CollaboratorDTO newCollaborator = collaboratorService.addCollaboratorByEmail(boardId, collaboratorCDTO.getUserEmail(), collaboratorCDTO.getAccessRight(), oidFromToken);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(newCollaborator);
//    }




}
