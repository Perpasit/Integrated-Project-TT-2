package sit.int221.intregratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.dtos.TaskCDTO;
import sit.int221.intregratedapi.dtos.TaskDDTO;
import sit.int221.intregratedapi.dtos.TaskRDTO;
import sit.int221.intregratedapi.dtos.TaskUDTO;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.exceptions.CustomErrorResponse;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;
import sit.int221.intregratedapi.utils.ListMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@NoArgsConstructor
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
    public TaskService(TaskRepository taskRepository, StatusRepository statusRepository, ListMapper listMapper) {
        this.taskRepository = taskRepository;
        this.statusRepository = statusRepository;
        this.listMapper = listMapper;
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public List<Task> getAllTasks(String sortBy, List<String> filterStatuses) {
        Sort sort;
        if (sortBy != null && sortBy.equalsIgnoreCase("asc")) {
            sort = Sort.by(Sort.Direction.ASC, "status.name");
        } else if (sortBy != null && sortBy.equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, "status.name");
        } else {
            sort = Sort.by("createdOn").ascending();
        }

        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            return taskRepository.findByStatusNameIn(filterStatuses, sort);
        } else {
            return taskRepository.findAll(sort);
        }
    }

    @Transactional
    public ResponseEntity<Object> createNewTask(TaskCDTO taskCDTO) {
        // Validate task title
        if (taskCDTO.getTitle() == null || taskCDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("{\"field\": \"title\", \"message\": \"is required\"}");
        }

        // Validate field sizes
        CustomErrorResponse response = validateFieldSize("title", taskCDTO.getTitle(), 100, null);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus()))
                    .body(response);
        }

        response = validateFieldSize("description", taskCDTO.getDescription(), 500, null);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus()))
                    .body(response);
        }

        response = validateFieldSize("assignees", taskCDTO.getAssignees(), 30, null);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus()))
                    .body(response);
        }

        // Validate status
        Status status;
        try {
            status = statusRepository.findById(Integer.valueOf(taskCDTO.getStatus()))
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + taskCDTO.getStatus() + " does not exist!"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("{\"field\": \"status\", \"message\": \"does not exist\"}");
        }

        Task task = modelMapper.map(taskCDTO, Task.class);

        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("The status has reached the limit.");
        }

        task.setStatus(status);

        setDefaultValuesForTask(task);
        Task createdTask = taskRepository.save(task);

        TaskCDTO createdTaskCDTO = modelMapper.map(createdTask, TaskCDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskCDTO);
    }

    @Transactional
    public ResponseEntity<TaskDDTO> removeTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!!"));
        taskRepository.delete(task);
        TaskDDTO taskDDTO = modelMapper.map(task, TaskDDTO.class);
        return ResponseEntity.ok(taskDDTO);
    }

    @Transactional
    public ResponseEntity<Object> updateTask(Integer taskId, TaskUDTO updatedTaskDTO) {
        Task existingTask = getTaskById(taskId);

        // Validate field sizes
        CustomErrorResponse response = validateFieldSize("title", updatedTaskDTO.getTitle(), 100, "/v2/tasks/" + taskId);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus()))
                    .body(response);
        }

        response = validateFieldSize("description", updatedTaskDTO.getDescription(), 500, "/v2/tasks/" + taskId);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus()))
                    .body(response);
        }

        response = validateFieldSize("assignees", updatedTaskDTO.getAssignees(), 30, "/v2/tasks/" + taskId);
        if (response != null) {
            return ResponseEntity.status(HttpStatus.valueOf(response.getStatus()))
                    .body(response);
        }

        // Validate status
        Status status;
        try {
            status = statusRepository.findById(Integer.valueOf(updatedTaskDTO.getStatus()))
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + updatedTaskDTO.getStatus() + " does not exist!"));
        } catch (NoSuchElementException e) {
            return ResponseEntity.badRequest().body("{\"field\": \"status\", \"message\": \"does not exist\"}");
        }

        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("The status has reached the limit.");
        }

        setDefaultValuesForTask(existingTask);
        existingTask.setStatus(status);
        existingTask.setTitle(updatedTaskDTO.getTitle());
        existingTask.setDescription(updatedTaskDTO.getDescription());
        existingTask.setAssignees(updatedTaskDTO.getAssignees());
        existingTask.setUpdatedOn(LocalDateTime.now());

        Task updatedTask = taskRepository.save(existingTask);
        TaskUDTO updatedTaskCDTO = modelMapper.map(updatedTask, TaskUDTO.class);
        return ResponseEntity.ok(updatedTaskCDTO);
    }

    public TaskRDTO getTaskDetailsById(Integer id) {
        Task task = getTaskById(id);
        return modelMapper.map(task, TaskRDTO.class);
    }

    private void setDefaultValuesForTask(Task task) {
        if (task.getTitle() == null) {
            throw new IllegalArgumentException("Task title is required");
        } else {
            task.setTitle(task.getTitle().trim());
        }

        task.setDescription((task.getDescription() == null || task.getDescription().trim().isEmpty()) ? null : task.getDescription().trim());

        task.setAssignees((task.getAssignees() == null || task.getAssignees().trim().isEmpty()) ? null : task.getAssignees().trim());

        if (task.getCreatedOn() == null) {
            task.setCreatedOn(LocalDateTime.now());
        }

        task.setUpdatedOn(LocalDateTime.now());
    }

    private CustomErrorResponse validateFieldSize(String field, String value, int maxSize, String path) {
        if (value != null && value.length() > maxSize) {
            return new CustomErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "size must be between 0 and " + maxSize,
                    path
            );
        }
        return null;
    }
}