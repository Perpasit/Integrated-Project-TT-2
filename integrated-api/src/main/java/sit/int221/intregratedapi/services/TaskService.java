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
import sit.int221.intregratedapi.repositories.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, ModelMapper modelMapper) {
        this.taskRepository = taskRepository;
        this.modelMapper = modelMapper;
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException(
                        "Task " + id + " does not exist !!!!")
        );
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public ResponseEntity<TaskCUDDTO> createNewTask(Task task) {
        if (task.getStatus() == null || task.getStatus().toString().trim().isEmpty()) {
            task.setStatus(TaskStatus.NO_STATUS);
        }

        task.setTitle(StringUtils.isEmpty(task.getTitle()) ? null : task.getTitle().trim());
        task.setDescription(StringUtils.isEmpty(task.getDescription()) ? null : task.getDescription().trim());
        task.setAssignees(StringUtils.isEmpty(task.getAssignees()) ? null : task.getAssignees().trim());

        Task savedTask = taskRepository.save(task);
        TaskCUDDTO postedTaskDTO = new TaskCUDDTO();
        postedTaskDTO.setId(savedTask.getId());
        postedTaskDTO.setTitle(savedTask.getTitle());
        postedTaskDTO.setDescription(savedTask.getDescription());
        postedTaskDTO.setAssignees(savedTask.getAssignees());
        postedTaskDTO.setStatus(savedTask.getStatus().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(postedTaskDTO);
    }


    @Transactional
    public ResponseEntity<TaskCUDDTO> removeTask(Integer taskId) {
        if (taskId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Task ID cannot be null");
        }
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!"));
        TaskCUDDTO deletedTaskDTO = new TaskCUDDTO();
        deletedTaskDTO.setId(task.getId());
        deletedTaskDTO.setTitle(task.getTitle());
        deletedTaskDTO.setDescription(task.getDescription());
        deletedTaskDTO.setAssignees(task.getAssignees());
        deletedTaskDTO.setStatus(task.getStatus().toString());
        taskRepository.delete(task);
        return ResponseEntity.ok(deletedTaskDTO);
    }

    @Transactional
    public TaskCUDDTO updateTask(Integer taskId, Task task) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!!"));
        existingTask.setTitle(StringUtils.isEmpty(task.getTitle()) ? null : task.getTitle().trim());
        existingTask.setDescription(StringUtils.isEmpty(task.getDescription()) ? null : task.getDescription().trim());
        existingTask.setAssignees(StringUtils.isEmpty(task.getAssignees()) ? null : task.getAssignees().trim());
        if (task.getStatus() == null || task.getStatus().toString().trim().isEmpty()) {
            existingTask.setStatus(TaskStatus.NO_STATUS);
        } else {
            existingTask.setStatus(task.getStatus());
        }
        Task updatedTask = taskRepository.save(existingTask);
        TaskCUDDTO updatedTaskDTO = new TaskCUDDTO();
        updatedTaskDTO.setId(updatedTask.getId());
        updatedTaskDTO.setTitle(updatedTask.getTitle());
        updatedTaskDTO.setDescription(updatedTask.getDescription());
        updatedTaskDTO.setAssignees(updatedTask.getAssignees());
        updatedTaskDTO.setStatus(updatedTask.getStatus().toString());
        return updatedTaskDTO;
    }
}
