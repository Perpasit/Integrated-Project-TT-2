package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.TaskCDTO;
import sit.int221.integratedapi.dtos.TaskDDTO;
import sit.int221.integratedapi.dtos.TaskRDTO;
import sit.int221.integratedapi.dtos.TaskUDTO;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.exceptions.CustomErrorResponse;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.repositories.StatusRepository;
import sit.int221.integratedapi.repositories.TaskRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public ResponseEntity<Object> getAllTasks(String sortBy, List<String> filterStatuses) {
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
            List<Integer> statusIds = extractStatusIds(filterStatuses);
            List<String> statusNames = extractStatusNames(filterStatuses);
            tasks = taskRepository.findByStatusIdInOrStatusNameIn(statusIds, statusNames, sort);
        } else {
            tasks = taskRepository.findAll(sort);
        }

        return ResponseEntity.ok(tasks);
    }

    private List<Integer> extractStatusIds(List<String> filterStatuses) {
        List<Integer> statusIds = new ArrayList<>();
        for (String status : filterStatuses) {
            try {
                int statusId = Integer.parseInt(status);
                statusIds.add(statusId);
            } catch (NumberFormatException e) {
                // Ignore non-numeric status values
            }
        }
        return statusIds;
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
        // Check if the title is null or empty
        if (taskCDTO.getTitle() == null || taskCDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("field", "title", "message", "is required"));
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
            return ResponseEntity.badRequest().body(Map.of("field", "status", "message", "does not exist"));
        }

        Task task = modelMapper.map(taskCDTO, Task.class);

        // Check status task limit
        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("The status " + status.getName() + " will have too many tasks. Please make progress and update status of existing tasks first.");
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

        // Check if the title is null or empty
        if (updatedTaskDTO.getTitle() == null || updatedTaskDTO.getTitle().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("field", "title", "message", "is required"));
        }

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
            return ResponseEntity.badRequest().body(Map.of("field", "status", "message", "does not exist"));
        }

        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body("The status " + status.getName() + " will have too many tasks.  Please make progress and update status of existing tasks first.");
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
