package sit.int221.integratedapi.controllers;

import org.springframework.core.io.Resource;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.entities.TaskAttachment;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.services.*;
import sit.int221.integratedapi.utils.ListMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class TaskController {
    private final TaskService taskService;
    private final ListMapper listMapper;

    private final JwtTokenUtil jwtTokenUtil;

    private final FileUploadService fileUploadService;

    private final FileDownloadService fileDownloadService;

    private final FileService fileService;


    public TaskController(TaskService taskService, ListMapper listMapper, JwtTokenUtil jwtTokenUtil, FileUploadService fileUploadService, FileDownloadService fileDownloadService, FileService fileService) {
        this.taskService = taskService;
        this.listMapper = listMapper;
        this.jwtTokenUtil = jwtTokenUtil;
        this.fileUploadService = fileUploadService;
        this.fileDownloadService = fileDownloadService;
        this.fileService = fileService;
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
        // taskService.validateBoardOwnershipForModification(boardId, oidFromToken);
        taskService.validateWriteAccess(boardId, oidFromToken);
        if (taskCDTO == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }
        return taskService.createNewTask(taskCDTO, boardId);
    }

    @PatchMapping("/{boardId}/tasks/{taskId}/attachments")
    public ResponseEntity<?> addAttachments(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        // Validate authorization
        String oidFromToken = jwtTokenUtil.getClaimFromToken(
                token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class)
        );
        if (oidFromToken == null) {
            throw new CustomAuthenticationException("Unauthorized. Invalid token.");
        }

        taskService.validateWriteAccess(boardId, oidFromToken);

        Task existingTask = taskService.findTaskById(taskId);
        if (existingTask == null) {
            throw new ItemNotFoundException("Task not found with ID: " + taskId);
        }

        // Check existing attachments count
        int existingFileCount = taskService.countAttachmentsByTaskId(taskId);
        final int MAX_FILES = 10;
        final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20 MB

        if (existingFileCount + files.length > MAX_FILES) {
            throw new MaxFilesExceededException("File limit exceeded. This task already has "
                    + existingFileCount + " files attached. You can upload up to "
                    + (MAX_FILES - existingFileCount) + " more files.");
        }

        for (MultipartFile file : files) {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new MaxFilesExceededException("File size exceeds the maximum limit of 20 MB.");
            }
        }

        try {
            List<TaskAttachmentDTO> savedAttachments = new ArrayList<>();
            for (MultipartFile file : files) {
                String filePath = fileUploadService.saveFile(file, boardId, taskId);

                TaskAttachment attachment = new TaskAttachment();
                attachment.setFileName(file.getOriginalFilename());
                attachment.setFilePath(filePath);
                attachment.setFileType(file.getContentType());
                attachment.setFileSize(file.getSize());
                attachment.setTask(existingTask);

                taskService.saveAttachment(
                        existingTask.getId(),
                        attachment.getFileName(),
                        attachment.getFilePath(),
                        attachment.getFileType(),
                        attachment.getFileSize()
                );

                TaskAttachmentDTO attachmentDTO = new TaskAttachmentDTO();
                attachmentDTO.setFileName(attachment.getFileName());
                attachmentDTO.setFilePath(attachment.getFilePath());
                attachmentDTO.setFileType(attachment.getFileType());
                attachmentDTO.setFileSize(attachment.getFileSize());

                savedAttachments.add(attachmentDTO);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(savedAttachments);

        } catch (IOException e) {
            throw new IllegalStateException("An error occurred while saving files: " + e.getMessage(), e);
        }
    }



    @GetMapping("/{boardId}/tasks/{taskId}/attachments")
    public ResponseEntity<?> getAttachments(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId) {

        // ตรวจสอบความถูกต้องของ token และการเข้าถึง board
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }

        taskService.validateBoardAccessForGet(boardId, oidFromToken);


        Task existingTask = taskService.findTaskById(taskId);
        if (existingTask == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Map<String, List<TaskAttachmentRDTO>> response = taskService.getAttachmentsByTaskId(taskId);
        if (response.get("attachments").isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{boardId}/tasks/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<TaskAttachmentRDTO> getAttachmentById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @PathVariable Integer attachmentId) {

        try {
            // ตรวจสอบ Token
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
            if (oidFromToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            taskService.validateBoardAccessForGet(boardId, oidFromToken);

            Task existingTask = taskService.findTaskById(taskId);
            if (existingTask == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            TaskAttachmentRDTO attachmentRDTO = taskService.findAttachmentById(attachmentId);

            return ResponseEntity.ok(attachmentRDTO);

        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping("/{boardId}/tasks/{taskId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @PathVariable Integer attachmentId) {
        try {
            // ตรวจสอบ Token
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
            if (oidFromToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            taskService.validateBoardAccessForGet(boardId, oidFromToken);

            String filePath = fileService.getFilePathById(attachmentId);
            String fileName = fileService.getFileNameById(attachmentId);

            Resource resource = fileDownloadService.downloadFile(filePath);

            String mimeType = fileDownloadService.determineMimeType(filePath);
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);

        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
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
        // taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        taskService.validateWriteAccess(boardId, oidFromToken);
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
        // taskService.validateBoardOwnershipForModification(boardId,oidFromToken);
        taskService.validateWriteAccess(boardId, oidFromToken);
        taskService.findTaskById(taskId);
        return taskService.removeTask(boardId, taskId);
    }


    @DeleteMapping("/{boardId}/tasks/{taskId}/attachments/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer taskId,
            @PathVariable Integer attachmentId) {
        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String oidFromToken = jwtTokenUtil.getClaimFromToken(
                    token.replace("Bearer ", ""),
                    claims -> claims.get("oid", String.class)
            );
            if (oidFromToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }


            taskService.validateWriteAccess(boardId, oidFromToken);


            Task existingTask = taskService.findTaskById(taskId);
            if (existingTask == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }


            TaskAttachmentRDTO deletedAttachmentRDTO = taskService.deleteAttachmentById(attachmentId);
            if (deletedAttachmentRDTO == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            try {
                fileUploadService.deleteFile(deletedAttachmentRDTO.getFilePath());
            } catch (IOException e) {
                System.err.println("Error deleting file: " + e.getMessage());
            }

            // Return the response
            return ResponseEntity.ok(deletedAttachmentRDTO);

        } catch (ItemNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



}
