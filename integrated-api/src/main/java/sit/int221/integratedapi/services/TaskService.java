package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.TaskCDTO;
import sit.int221.integratedapi.dtos.TaskDDTO;
import sit.int221.integratedapi.dtos.TaskRDTO;
import sit.int221.integratedapi.dtos.TaskUDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.StatusRepository;
import sit.int221.integratedapi.repositories.primary.TaskRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
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
    TaskUserRepository taskUserRepository;

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public TaskRDTO getTaskDetailsById(Integer boardId, Integer taskId) {
        Task task = getTaskById(taskId);

        if (!Objects.equals(boardId, task.getBoard().getId())) {
            throw new ItemNotFoundException("Task " + taskId + " does not belong to board " + boardId);
        }

        return modelMapper.map(task, TaskRDTO.class);
    }

    public ResponseEntity<Object> getAllTasks(String sortBy, List<String> filterStatuses, List<String> allParams) {
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
            tasks = taskRepository.findByStatusNameIn(statusNames, sort);
        } else {
            tasks = taskRepository.findAll(sort);
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
    public ResponseEntity<Object> createNewTask(TaskCDTO taskCDTO) {
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        if (taskCDTO.getTitle() == null || taskCDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null");
        }

        if (taskCDTO.getBoardId() == null) {
            validationErrors.addError("boardId", "must not be null");
        }

        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        Status status = getDefaultStatus();
        if (taskCDTO.getStatus() != null && !taskCDTO.getStatus().trim().isEmpty()) {
            try {
                status = statusRepository.findById(Integer.valueOf(taskCDTO.getStatus()))
                        .orElseThrow(() -> new ItemNotFoundException("Status with id " + taskCDTO.getStatus() + " does not exist!"));
            } catch (NoSuchElementException e) {
                validationErrors.addError("status", "does not exist");
                throw validationErrors;
            }
        }

        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + status.getName() + " will have too many tasks. Please make progress and update the status of existing tasks first.");
        }

        // ตรวจสอบความถูกต้องของ boardId
        Board board = boardRepository.findById(taskCDTO.getBoardId())
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + taskCDTO.getBoardId() + " does not exist!"));

        Task task = new Task();
        task.setTitle(taskCDTO.getTitle().trim());
        task.setStatus(status);
        task.setDescription(taskCDTO.getDescription() != null && !taskCDTO.getDescription().trim().isEmpty() ? taskCDTO.getDescription().trim() : null);
        task.setAssignees(taskCDTO.getAssignees() != null && !taskCDTO.getAssignees().trim().isEmpty() ? taskCDTO.getAssignees().trim() : null);
        task.setBoard(board); // ตั้งค่า board
        setDefaultValuesForTask(task);
        Task createdTask = taskRepository.save(task);
        TaskCDTO createdTaskCDTO = modelMapper.map(createdTask, TaskCDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskCDTO);
    }





    @Transactional
    public ResponseEntity<TaskDDTO> removeTask(Integer boardId, Integer taskId) {
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
    public ResponseEntity<Object> updateTask(Integer boardId, Integer taskId, TaskUDTO updatedTaskDTO) {
        // ตรวจสอบว่ามี Task ที่ต้องการหรือไม่
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist!"));

        // ตรวจสอบว่า Task ที่จะอัปเดตเป็นของ Board ที่ถูกต้อง
        if (existingTask.getBoard() == null || !Objects.equals(existingTask.getBoard().getId(), boardId)) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not exist!");
        }

        // ตรวจสอบความถูกต้องของข้อมูล
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        if (updatedTaskDTO.getTitle() == null || updatedTaskDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null");
        }

        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        // ตรวจสอบ Status และจัดการข้อผิดพลาด
        Status status = getDefaultStatus();
        if (updatedTaskDTO.getStatus() != null && !updatedTaskDTO.getStatus().trim().isEmpty()) {
            try {
                status = statusRepository.findById(Integer.valueOf(updatedTaskDTO.getStatus()))
                        .orElseThrow(() -> new ItemNotFoundException("Status with id " + updatedTaskDTO.getStatus() + " does not exist!"));
            } catch (NumberFormatException e) {
                validationErrors.addError("status", "invalid status format");
                throw validationErrors;
            } catch (NoSuchElementException e) {
                validationErrors.addError("status", "does not exist");
                throw validationErrors;
            }
        }

        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + status.getName() + " will have too many tasks. Please make progress and update the status of existing tasks first.");
        }

        // ตรวจสอบและตั้งค่า Board
        if (updatedTaskDTO.getBoardId() != null) {
            Board board = boardRepository.findById(updatedTaskDTO.getBoardId())
                    .orElseThrow(() -> new ItemNotFoundException("Board with id " + updatedTaskDTO.getBoardId() + " does not exist!"));
            existingTask.setBoard(board);
        }

        // อัปเดต Task ด้วยข้อมูลใหม่
        existingTask.setTitle(updatedTaskDTO.getTitle().trim());
        existingTask.setDescription(updatedTaskDTO.getDescription() != null && !updatedTaskDTO.getDescription().trim().isEmpty() ? updatedTaskDTO.getDescription().trim() : null);
        existingTask.setAssignees(updatedTaskDTO.getAssignees() != null && !updatedTaskDTO.getAssignees().trim().isEmpty() ? updatedTaskDTO.getAssignees().trim() : null);
        existingTask.setStatus(status);
        existingTask.setUpdatedOn(ZonedDateTime.now());

        // บันทึก Task ที่อัปเดตแล้ว
        Task updatedTask = taskRepository.save(existingTask);
        TaskUDTO updatedTaskCDTO = modelMapper.map(updatedTask, TaskUDTO.class);

        // ส่งผลลัพธ์กลับ
        return ResponseEntity.ok(updatedTaskCDTO);
    }


    private Status getDefaultStatus() {
        return statusRepository.findByNameIgnoreCase("no status")
                .orElseThrow(() -> new IllegalStateException("Default 'no status' not found in the database"));
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

    public ResponseEntity<Object> getTasksByBoardId(Integer boardId, String sortBy, List<String> filterStatuses) {
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
        List<String> statusNames = null;
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            statusNames = extractStatusNames(filterStatuses);
            tasks = taskRepository.findByStatusNameIn(statusNames, sort);
        } else {
            tasks = taskRepository.findByBoardIdAndStatusNameIn(boardId, statusNames, sort);
        }

        // Return the result
        return ResponseEntity.ok(tasks);
    }


    public boolean isBoardOwnedByOid(Integer boardId, String oidFromToken) {
        // Fetch the board by ID
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        // If board is not found, return false
        if (!boardOptional.isPresent()) {
            return false;
        }

        Board board = boardOptional.get();

        // Fetch the TaskUser associated with the board's taskusersOid
        Optional<TaskUser> taskUserOptional = Optional.ofNullable(taskUserRepository.findByOid(board.getTaskusersOid()));

        // If the taskUser is found, check if it matches the OID
        return taskUserOptional.isPresent() && taskUserOptional.get().getOid().equals(oidFromToken);
    }


    // Method to fetch tasks by boardId
    public List<TaskRDTO> getTasksByBoardId(Integer boardId) {
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



}