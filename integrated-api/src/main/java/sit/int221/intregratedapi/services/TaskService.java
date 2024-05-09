package sit.int221.intregratedapi.services;


import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import sit.int221.intregratedapi.dtos.TaskCUDDTO;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.entities.TaskStatus;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final StatusRepository statusRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, StatusRepository statusRepository, ModelMapper modelMapper) {
        this.taskRepository = taskRepository;
        this.statusRepository = statusRepository;
        this.modelMapper = modelMapper;
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException("Task " + id + " does not exist !!!!")
        );
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public ResponseEntity<TaskCUDDTO> createNewTask(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(statusRepository.findByName("No Status").orElse(null)); // Ensure "No Status" is predefined in your statuses table
        }

        task.setTitle(StringUtils.isEmpty(task.getTitle()) ? null : task.getTitle().trim());
        task.setDescription(StringUtils.isEmpty(task.getDescription()) ? null : task.getDescription().trim());
        task.setAssignees(StringUtils.isEmpty(task.getAssignees()) ? null : task.getAssignees().trim());

        Task savedTask = taskRepository.save(task);
        TaskCUDDTO postedTaskDTO = modelMapper.map(savedTask, TaskCUDDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(postedTaskDTO);
    }

    @Transactional
    public ResponseEntity<TaskCUDDTO> removeTask(Integer taskId) {
        if (taskId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Task ID cannot be null");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!"));
        TaskCUDDTO deletedTaskDTO = modelMapper.map(task, TaskCUDDTO.class);
        taskRepository.delete(task);
        return ResponseEntity.ok(deletedTaskDTO);
    }

<<<<<<< Updated upstream
    @Transactional
    public TaskCUDDTO updateTask(Integer taskId, Task taskDetails) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!!"));
        existingTask.setTitle(StringUtils.isEmpty(taskDetails.getTitle()) ? null : taskDetails.getTitle().trim());
        existingTask.setDescription(StringUtils.isEmpty(taskDetails.getDescription()) ? null : taskDetails.getDescription().trim());
        existingTask.setAssignees(StringUtils.isEmpty(taskDetails.getAssignees()) ? null : taskDetails.getAssignees().trim());
        if (taskDetails.getStatus() != null) {
            existingTask.setStatus(taskDetails.getStatus());
        }
        Task updatedTask = taskRepository.save(existingTask);
        return modelMapper.map(updatedTask, TaskCUDDTO.class);
    }

}
=======
    private TaskRDTO toTaskRDTO(Task task) {
        TaskRDTO taskRDTO = TaskRDTO.builder()
                .id(task.getId().toString())
                .title(new StringBuilder(StringUtils.isEmpty(task.getTaskTitle()) ? "" : task.getTaskTitle())) // Check for null
                .description(new StringBuilder(StringUtils.isEmpty(task.getTaskDescription()) ? "" : task.getTaskDescription())) // Check for null
                .assignees(new StringBuilder(StringUtils.isEmpty(task.getTaskAssignees()) ? "" : task.getTaskAssignees())) // Check for null
                .status(statusRepository.findById(task.getStatus().getId())
                        .map(Status::getStatusName)
                        .orElse("No Status"))
                .createdOn(task.getCreatedOn().toString())
                .updatedOn(task.getUpdatedOn().toString())
                .build();
        return taskRDTO;
    }


}
>>>>>>> Stashed changes
