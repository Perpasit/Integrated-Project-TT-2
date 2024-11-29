package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.*;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.repositories.login.UserRepository;
import sit.int221.integratedapi.repositories.primary.*;
import sit.int221.integratedapi.utils.ListMapper;


import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Qualifier("primaryDataSource")
@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StatusService statusService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    private FileUploadService fileUploadService;

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public TaskRDTO getTaskDetailsById(String boardId, Integer taskId) {
        Task task = getTaskById(taskId);

        if (!Objects.equals(boardId, task.getBoard().getId())) {
            throw new ItemNotFoundException("Task " + taskId + " does not belong to board " + boardId);
        }

        return modelMapper.map(task, TaskRDTO.class);
    }

    public void validateBoardAccessForGet(String boardId, String oidFromToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // If the board is public, allow access
        if (board.getVisibility() == Board.Visibility.PUBLIC) {
            return;
        }

        // If the token is missing or oid is null, deny access
        if (oidFromToken == null) {
            throw new ForbiddenException("Access denied. You must be logged in to view this board.");
        }

        // If the board is private, check if the user has access
        if (!board.getOwnerId().equals(oidFromToken)) {
            User user = userRepository.findByOid(oidFromToken);
            if (user == null) {
                throw new ItemNotFoundException("User with id " + oidFromToken + " not found");
            }

            Collaborator collaborator = collaboratorRepository.findByUserOidAndBoard(user.getOid(), board);
            if (collaborator == null ||
                    (collaborator.getAccessRight() != Collaborator.AccessRight.READ &&
                            collaborator.getAccessRight() != Collaborator.AccessRight.WRITE)) {
                throw new ForbiddenException("You do not have permission to access this board");
            }
        }
    }

    public void validateBoardOwnershipForModification(String boardId, String oidFromToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        if (oidFromToken == null || !board.getOwnerId().equals(oidFromToken)) {
            throw new ForbiddenException("You do not have permission to modify this board.");
        }
    }

    public void validateWriteAccess(String boardId, String oidFromToken) {
        // ค้นหาบอร์ดที่ต้องการเข้าถึง
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // ตรวจสอบว่า oidFromToken เป็นเจ้าของบอร์ดหรือเป็น collaborator ที่มีสิทธิ์ WRITE
        if (!board.getOwnerId().equals(oidFromToken)) {
            User user = userRepository.findByOid(oidFromToken);
            if (user == null) {
                throw new ItemNotFoundException("User with id " + oidFromToken + " not found");
            }

            Collaborator collaborator = collaboratorRepository.findByUserOidAndBoard(user.getOid(), board);
            if (collaborator == null || collaborator.getAccessRight() != Collaborator.AccessRight.WRITE) {
                throw new ForbiddenException("You do not have permission to modify this board");
            }
        }
    }




    public Task findTaskById(Integer taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist!"));
    }

    public ResponseEntity<Object> getAllTasks(String boardId, String sortBy, List<String> filterStatuses, List<String> allParams) {
        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        Sort sort;
        if (sortBy != null && sortBy.equalsIgnoreCase("asc")) {
            sort = Sort.by(Sort.Direction.ASC, "status.name");
        } else if (sortBy != null && sortBy.equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, "status.name");
        } else {
            sort = Sort.by("createdOn").ascending();
        }

        List<Task> tasks;
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            List<String> statusNames = extractStatusNames(filterStatuses);
            tasks = taskRepository.findByStatusNameInAndBoardId(statusNames, boardId, sort);
        } else {
            tasks = taskRepository.findByBoardId(boardId, sort);
        }
        return ResponseEntity.ok(tasks);
    }

    private List<String> extractStatusNames(List<String> filterStatuses) {
        List<String> statusNames = new ArrayList<>();
        for (String status : filterStatuses) {
            if (!status.matches("\\d+")) {
                statusNames.add(status);
            }
        }
        return statusNames;
    }



    @Transactional
    public ResponseEntity<Object> createNewTask(TaskCDTO taskCDTO, String boardId) {
        // Create ValidationErrorException to check validation
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        // Check if the title is provided
        if (taskCDTO.getTitle() == null || taskCDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null or empty");
        }

        // Check if boardId is provided
        if (boardId == null) {
            validationErrors.addError("boardId", "must not be null");
        }

        // Throw exception if there are validation errors
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        // Find the Board by boardId
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // Retrieve the status from taskCDTO, or use default status if not provided
        Status status;
        if (taskCDTO.getStatus() != null && !taskCDTO.getStatus().trim().isEmpty()) {
            try {
                Integer statusId = Integer.valueOf(taskCDTO.getStatus().trim());
                status = statusRepository.findByIdAndBoardId(statusId, boardId)
                        .orElseThrow(() -> new ItemNotFoundException("Status with id " + statusId + " does not exist for board id " + boardId));
            } catch (NumberFormatException e) {
                validationErrors.addError("status", "must be a valid number");
                throw validationErrors;
            }
        } else {
            // Use default 'no status' if no status provided
            status = getDefaultStatus(boardId);
        }

        // Retrieve the status name
        String statusName = status.getName(); // Assuming Status entity has a getName() method

        // Check if the status has a task limit and if it's reached
        if (status.getGlobalSetting().isLimitMaximumTask() &&
                taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + statusName + " has reached its maximum task limit.");
        }

        // Create a new Task
        Task task = new Task();
        task.setTitle(taskCDTO.getTitle().trim());
        task.setStatus(status);  // Set the status entity
        task.setDescription(taskCDTO.getDescription() != null && !taskCDTO.getDescription().trim().isEmpty() ? taskCDTO.getDescription().trim() : null);
        task.setAssignees(taskCDTO.getAssignees() != null && !taskCDTO.getAssignees().trim().isEmpty() ? taskCDTO.getAssignees().trim() : null);
        task.setBoard(board);  // Set the board
        setDefaultValuesForTask(task);  // Set default values

        // Save the task to the database
        Task createdTask = taskRepository.save(task);

        // Manually map Task to TaskCDTO
        TaskCDTO createdTaskCDTO = new TaskCDTO();
        createdTaskCDTO.setId(createdTask.getId());
        createdTaskCDTO.setTitle(createdTask.getTitle());
        createdTaskCDTO.setDescription(createdTask.getDescription());
        createdTaskCDTO.setAssignees(createdTask.getAssignees());
        createdTaskCDTO.setStatus(statusName);  // Use statusName instead of statusId
        createdTaskCDTO.setBoardId(createdTask.getBoard() != null ? createdTask.getBoard().getId() : null);

        // Respond with HTTP Status 201 and the created task data
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskCDTO);
    }



    @Transactional
    public ResponseEntity<TaskDDTO> removeTask(String boardId, Integer taskId) {
        // Fetch the task from the repository
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist!"));

        // Verify that the task belongs to the specified board
        if (task.getBoard() == null || !Objects.equals(task.getBoard().getId(), boardId)) {
            throw new ItemNotFoundException("Task with id " + taskId + " does not belong to board with id " + boardId + " or does not exist.");
        }

        // Delete the task
        taskRepository.delete(task);

        // Map the deleted task to DTO
        TaskDDTO taskDDTO = modelMapper.map(task, TaskDDTO.class);

        // Return a response with the details of the deleted task
        return ResponseEntity.ok(taskDDTO);
    }



    @Transactional
    public ResponseEntity<Object> updateTask(String boardId, Integer taskId, TaskUDTO updatedTaskDTO) {
        // Check if the Task exists
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist!"));

        // Verify if the Task belongs to the correct Board
        if (existingTask.getBoard() == null || !Objects.equals(existingTask.getBoard().getId(), boardId)) {
            throw new ItemNotFoundException("Task does not belong to Board with id " + boardId + "!");
        }

        // Create ValidationErrorException for validation checks
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        // Validate the title
        if (updatedTaskDTO.getTitle() == null || updatedTaskDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null or empty");
        }

        // Throw exception if there are validation errors
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        // Handle Status
        Status status = getDefaultStatus(boardId); // Default status for the board

        if (updatedTaskDTO.getStatus() != null && !updatedTaskDTO.getStatus().trim().isEmpty()) {
            try {
                // Find status by ID
                Integer statusId = Integer.valueOf(updatedTaskDTO.getStatus().trim());
                status = statusRepository.findByIdAndBoardId(statusId, boardId)
                        .orElseThrow(() -> new ItemNotFoundException("Status with id " + statusId + " does not exist for board id " + boardId));
            } catch (NumberFormatException e) {
                validationErrors.addError("status", "must be a valid number");
                throw
                        validationErrors;
            }
        }

        // Retrieve the status name
        String statusName = status.getName(); // Assuming Status entity has a getName() method

        // Check if the status has reached its task limit
        if (status.getGlobalSetting().isLimitMaximumTask() &&
                taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + statusName + " has reached its maximum task limit.");
        }

        // Update Task with new data
        existingTask.setTitle(updatedTaskDTO.getTitle().trim());
        existingTask.setDescription(updatedTaskDTO.getDescription() != null && !updatedTaskDTO.getDescription().trim().isEmpty() ? updatedTaskDTO.getDescription().trim() : null);
        existingTask.setAssignees(updatedTaskDTO.getAssignees() != null && !updatedTaskDTO.getAssignees().trim().isEmpty() ? updatedTaskDTO.getAssignees().trim() : null);
        existingTask.setStatus(status);  // Update status
        existingTask.setUpdatedOn(ZonedDateTime.now());

        // Save updated Task
        Task updatedTask = taskRepository.save(existingTask);

        // Map updated Task to DTO
        TaskUDTO updatedTaskCDTO = modelMapper.map(updatedTask, TaskUDTO.class);
        updatedTaskCDTO.setStatus(statusName); // Set the status name in the DTO

        // Return updated Task
        return ResponseEntity.ok(updatedTaskCDTO);
    }






    private Status getDefaultStatus(String boardId) {
        return statusRepository.findByNameIgnoreCaseAndBoardId("no status", boardId)
                .orElseThrow(() -> new IllegalStateException("Default 'no status' for board " + boardId + " not found in the database"));
    }


    private void setDefaultValuesForTask(Task task) {
        task.setTitle(task.getTitle().trim());
        task.setDescription(task.getDescription() != null && !task.getDescription().trim().isEmpty() ? task.getDescription().trim() : null);
        task.setAssignees(task.getAssignees() != null && !task.getAssignees().trim().isEmpty() ? task.getAssignees().trim() : null);
        if (task.getCreatedOn() == null) {
            task.setCreatedOn(ZonedDateTime.now());
        }
        task.setUpdatedOn(ZonedDateTime.now());
    }

    public ResponseEntity<Object> getTasksByBoardId(String boardId, String sortBy, List<String> filterStatuses) {
        // Return an empty list if any status contains '#'
        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // Determine sorting direction based on the sortBy parameter
        Sort sort;
        if (sortBy != null && sortBy.equalsIgnoreCase("asc")) {
            sort = Sort.by(Sort.Direction.ASC, "status.name");
        } else if (sortBy != null && sortBy.equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, "status.name");
        } else {
            sort = Sort.by("createdOn").ascending();  // Default sorting by creation date
        }

        // Retrieve tasks by boardId with optional status filtering
        List<Task> tasks;

        // Extract status names if provided, or fetch all tasks if filterStatuses is null or empty
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            tasks = taskRepository.findByBoardIdAndStatusNameIn(boardId, filterStatuses, sort);
        } else {
            tasks = taskRepository.findByBoardId(boardId, sort);
        }

        // Return the result
        return ResponseEntity.ok(tasks);
    }



    public boolean isBoardOwnedByOid(String boardId, String oidFromToken) {
        // Fetch the board by ID
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        // If board is not found, return false
        if (!boardOptional.isPresent()) {
            return false;
        }

        Board board = boardOptional.get();

        // Fetch the TaskUser associated with the board's taskusersOid
        Optional<User> taskUserOptional = Optional.ofNullable(userRepository.findByOid(board.getOwnerId()));

        // If the taskUser is found, check if it matches the OID
        return taskUserOptional.isPresent() && taskUserOptional.get().getOid().equals(oidFromToken);
    }


    // Method to fetch tasks by boardId
    public List<TaskRDTO> getTasksByBoardId(String boardId) {
        // Fetch tasks by boardId (directly, without Optional)
        List<Task> tasks = taskRepository.findByBoardId(boardId);

        // Convert to TaskRDTOs
        List<TaskRDTO> taskRDTOs = new ArrayList<>();
        for (Task task : tasks) {
            TaskRDTO taskRDTO = new TaskRDTO();
            taskRDTO.setTitle(task.getTitle());
            taskRDTO.setDescription(task.getDescription());
            taskRDTO.setStatus(String.valueOf(task.getStatus()));
            // Populate other TaskRDTO fields as necessary
            taskRDTOs.add(taskRDTO);
        }

        return taskRDTOs;
    }


    @Transactional
    public void saveAttachment(Integer taskId, String fileName, String filePath, String fileType, Long fileSize) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist"));

        TaskAttachment attachment = new TaskAttachment();
        attachment.setFileName(fileName);
        attachment.setFilePath(filePath);
        attachment.setFileType(fileType);
        attachment.setFileSize(fileSize);
        attachment.setTask(task);

        taskAttachmentRepository.save(attachment);
    }

    public Map<String, List<TaskAttachmentRDTO>> getAttachmentsByTaskId(Integer taskId) {
        List<TaskAttachment> attachments = taskAttachmentRepository.findByTaskId(taskId);

        List<TaskAttachmentRDTO> attachmentRDTOs = attachments.stream().map(attachment -> {
            TaskAttachmentRDTO dto = new TaskAttachmentRDTO();
            dto.setId(attachment.getId());
            dto.setFileName(attachment.getFileName());
            dto.setFilePath(attachment.getFilePath());
            dto.setFileType(attachment.getFileType());
            dto.setFileSize(attachment.getFileSize());


            return dto;
        }).collect(Collectors.toList());

        Map<String, List<TaskAttachmentRDTO>> response = new HashMap<>();
        response.put("attachments", attachmentRDTOs);

        return response;
    }





    @Transactional
    public TaskAttachmentRDTO deleteAttachmentById(Integer attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ItemNotFoundException("Attachment with id " + attachmentId + " does not exist"));
        
        TaskAttachmentRDTO attachmentRDTO = new TaskAttachmentRDTO();
        attachmentRDTO.setId(attachment.getId());
        attachmentRDTO.setFileName(attachment.getFileName());
        attachmentRDTO.setFilePath(attachment.getFilePath());
        attachmentRDTO.setFileType(attachment.getFileType());
        attachmentRDTO.setFileSize(attachment.getFileSize());


        taskAttachmentRepository.deleteById(attachmentId);

        return attachmentRDTO;
    }


    @Transactional
    public TaskAttachmentRDTO findAttachmentById(Integer attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ItemNotFoundException("Attachment with id " + attachmentId + " does not exist"));

        TaskAttachmentRDTO attachmentRDTO = new TaskAttachmentRDTO();
        attachmentRDTO.setId(attachment.getId());
        attachmentRDTO.setFileName(attachment.getFileName());
        attachmentRDTO.setFilePath(attachment.getFilePath());
        attachmentRDTO.setFileType(attachment.getFileType());
        attachmentRDTO.setFileSize(attachment.getFileSize());

        return attachmentRDTO;
    }








}