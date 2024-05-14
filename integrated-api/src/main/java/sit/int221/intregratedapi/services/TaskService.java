package sit.int221.intregratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.dtos.*;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;
import sit.int221.intregratedapi.utils.ListMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public TaskService(TaskRepository taskRepository, StatusRepository statusRepository, ListMapper listMapper) {
        this.taskRepository = taskRepository;
        this.statusRepository = statusRepository;
        this.listMapper = listMapper;
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public ResponseEntity<TaskCDTO> createNewTask(Task task) {
        setDefaultValuesForTask(task);
        taskRepository.save(task);
        TaskCDTO taskCDTO = toTaskCUDDTO(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskCDTO);
    }

    @Transactional
    public ResponseEntity<TaskDDTO> removeTask(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!!"));
        taskRepository.delete(task);
        TaskDDTO taskDDTO = toTaskDDTO(task);
        return ResponseEntity.ok(taskDDTO);
    }

    @Transactional
    public ResponseEntity<TaskUDTO> updateTask(Integer taskId, Task task) {
        Task existingTask = getTaskById(taskId);
        setDefaultValuesForTask(task);
        updateTaskFromDTO(existingTask, task);
        Task updatedTask = taskRepository.save(existingTask);
        TaskUDTO taskUDTO = toTaskUDTO(updatedTask);
        return ResponseEntity.ok(taskUDTO);
    }

    public TaskRDTO getTaskRDTOById(Integer id) {
        Task task = getTaskById(id);
        return toTaskRDTO(task);
    }

    private void setDefaultValuesForTask(Task task) {
        if (task.getStatus() == null || task.getStatus().getId() == null) {
            Status defaultStatus = statusRepository.findByStatusNameIgnoreCase("NO_STATUS")
                    .orElseGet(() -> statusRepository.save(Status.builder()
                            .statusName("NO_STATUS")
                            .build()));
            task.setStatus(defaultStatus);
        }

        if (task.getTaskTitle() == null) {
            throw new IllegalArgumentException("Task title is required");
        } else {
            task.setTaskTitle(task.getTaskTitle().trim());
        }

        if (task.getTaskDescription() != null) {
            task.setTaskDescription(task.getTaskDescription().trim());
        }

        if (task.getTaskAssignees() != null) {
            task.setTaskAssignees(task.getTaskAssignees().trim());
        }

        if (task.getCreatedOn() == null) {
            task.setCreatedOn(LocalDateTime.now());
        }
        task.setUpdatedOn(LocalDateTime.now());
    }

    private void updateTaskFromDTO(Task existingTask, Task task) {
        existingTask.setTaskTitle(task.getTaskTitle());
        existingTask.setTaskDescription(task.getTaskDescription());
        existingTask.setTaskAssignees(task.getTaskAssignees());
        existingTask.setStatus(Optional.ofNullable(task.getStatus()).orElseGet(() -> {
            Status defaultStatus = statusRepository.findByStatusNameIgnoreCase("NO_STATUS")
                    .orElseGet(() -> statusRepository.save(Status.builder()
                            .statusName("NO_STATUS")
                            .build()));
            return defaultStatus;
        }));
        existingTask.setUpdatedOn(LocalDateTime.now());
    }

    private TaskDTO toTaskDTO(Task task) {
        return TaskDTO.builder()
                .id(task.getId().toString())
                .title(new StringBuilder(task.getTaskTitle()))
                .assignees(new StringBuilder(task.getTaskAssignees() == null ? "" : task.getTaskAssignees()))
                .status(statusRepository.findById(task.getStatus().getId())
                        .map(Status::getStatusName)
                        .orElse("NO_STATUS"))
                .build();
    }

    private TaskRDTO toTaskRDTO(Task task) {
        return TaskRDTO.builder()
                .id(task.getId().toString())  // Again, consider whether you need the ID as a string.
                .title(new StringBuilder(task.getTaskTitle()))
                .description(isNullOrEmpty(task.getTaskDescription()) ? null : new StringBuilder(task.getTaskDescription()))
                .assignees(isNullOrEmpty(task.getTaskAssignees()) ? null : new StringBuilder(task.getTaskAssignees()))
                .status(task.getStatus().getStatusName())
                .createdOn(task.getCreatedOn().toString())
                .updatedOn(task.getUpdatedOn().toString())
                .build();
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }



    private TaskCDTO toTaskCUDDTO(Task task) {
        String statusName = (task.getStatus() != null && task.getStatus().getId() != null)
                ? statusRepository.findById(task.getStatus().getId())
                .map(Status::getStatusName)
                .orElse("NO_STATUS")
                : "NO_STATUS";

        return TaskCDTO.builder()
                .id(task.getId().toString())
                .title(task.getTaskTitle())
                .description(task.getTaskDescription() != null ? task.getTaskDescription() : "")
                .assignees(task.getTaskAssignees() != null ? task.getTaskAssignees() : "")
                .status(statusName)
                .build();
    }
    private TaskDDTO toTaskDDTO(Task task) {
        return TaskDDTO.builder()
                .id(task.getId().toString())
                .title(task.getTaskTitle())
                .assignees(task.getTaskAssignees())
                .status(task.getStatus().getStatusName())
                .build();
    }

    private TaskUDTO toTaskUDTO(Task task) {
        String statusName;
        if (task.getStatus() != null && task.getStatus().getId() != null) {
            Status status = statusRepository.findById(task.getStatus().getId())
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + task.getStatus().getId() + " does not exist!"));
            statusName = status.getStatusName();
        } else {
            statusName = "No Status";
        }

        return TaskUDTO.builder()
                .title(task.getTaskTitle())
                .description(task.getTaskDescription() != null ? task.getTaskDescription() : "")
                .assignees(task.getTaskAssignees() != null ? task.getTaskAssignees() : "")
                .status(statusName)
                .build();
    }
}