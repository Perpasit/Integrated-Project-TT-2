package sit.int221.intregratedapi.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import sit.int221.intregratedapi.dtos.TaskRDTO;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;

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
    public TaskService(TaskRepository taskRepository, StatusRepository statusRepository) {
        this.taskRepository = taskRepository;
        this.statusRepository = statusRepository;
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public ResponseEntity<TaskRDTO> createNewTaskAndGetRDTO(Task task) {
        setDefaultValuesForTask(task);
        Task savedTask = taskRepository.save(task);
        TaskRDTO taskRDTO = toTaskRDTO(savedTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskRDTO);
    }

    @Transactional
    public ResponseEntity<TaskRDTO> removeTaskAndGetRDTO(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!!"));
        taskRepository.delete(task);
        TaskRDTO taskRDTO = toTaskRDTO(task);
        return ResponseEntity.ok(taskRDTO);
    }

    @Transactional
    public ResponseEntity<TaskRDTO> updateTaskAndGetRDTO(Integer taskId, Task task) {
        Task existingTask = getTaskById(taskId);
        setDefaultValuesForTask(task);
        updateTaskFromDTO(existingTask, task);
        Task updatedTask = taskRepository.save(existingTask);
        TaskRDTO taskRDTO = toTaskRDTO(updatedTask);
        return ResponseEntity.ok(taskRDTO);
    }

    public TaskRDTO getTaskRDTOById(Integer id) {
        Task task = getTaskById(id);
        return toTaskRDTO(task);
    }

    private void setDefaultValuesForTask(Task task) {
        if (task.getStatus() == null) {
            Status defaultStatus = statusRepository.findByStatusNameIgnoreCase("No Status")
                    .orElseGet(() -> statusRepository.save(Status.builder()
                            .statusName("No Status")
                            .build()));
            task.setStatus(defaultStatus);
        }
        task.setTaskTitle(StringUtils.isEmpty(task.getTaskTitle()) ? null : task.getTaskTitle().trim());
        task.setTaskDescription(StringUtils.isEmpty(task.getTaskDescription()) ? null : task.getTaskDescription().trim());
        task.setTaskAssignees(StringUtils.isEmpty(task.getTaskAssignees()) ? null : task.getTaskAssignees().trim());
        task.setCreatedOn(LocalDateTime.now());
        task.setUpdatedOn(LocalDateTime.now());
    }

    private void updateTaskFromDTO(Task existingTask, Task task) {
        existingTask.setTaskTitle(task.getTaskTitle());
        existingTask.setTaskDescription(task.getTaskDescription());
        existingTask.setTaskAssignees(task.getTaskAssignees());
        existingTask.setStatus(Optional.ofNullable(task.getStatus()).orElseGet(() -> {
            Status defaultStatus = statusRepository.findByStatusNameIgnoreCase("No Status")
                    .orElseGet(() -> statusRepository.save(Status.builder()
                            .statusName("No Status")
                            .build()));
            return defaultStatus;
        }));
        existingTask.setUpdatedOn(LocalDateTime.now());
    }

    private TaskRDTO toTaskRDTO(Task task) {
        TaskRDTO taskRDTO = TaskRDTO.builder()
                .id(task.getId().toString())
                .title(new StringBuilder(task.getTaskTitle()))
                .description(new StringBuilder(task.getTaskDescription()))
                .assignees(new StringBuilder(task.getTaskAssignees()))
                .status(statusRepository.findById(task.getStatus().getId())
                        .map(Status::getStatusName)
                        .orElse("No Status"))
                .createdOn(task.getCreatedOn().toString())
                .updatedOn(task.getUpdatedOn().toString())
                .build();
        return taskRDTO;
    }
}