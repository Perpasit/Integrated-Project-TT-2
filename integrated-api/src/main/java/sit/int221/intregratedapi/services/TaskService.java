package sit.int221.intregratedapi.services;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.intregratedapi.dtos.TaskDTO;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.TaskRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity<TaskDTO> createNewTask(Task task) {
        Task savedTask = taskRepository.save(task);

        TaskDTO postedTaskDTO = new TaskDTO();
        postedTaskDTO.setId(savedTask.getId().toString());
        postedTaskDTO.setTitle(savedTask.getTitle());
        postedTaskDTO.setDescription(savedTask.getDescription());
        postedTaskDTO.setAssignees(savedTask.getAssignees());
        postedTaskDTO.setStatus(savedTask.getStatus().toString());

        return ResponseEntity.ok(postedTaskDTO);
    }

    @Transactional
    public ResponseEntity<TaskDTO> removeTask(Integer taskId) {
        if (taskId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Task ID cannot be null");
        }

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!"));

        TaskDTO deletedTaskDTO = new TaskDTO();
        deletedTaskDTO.setId(task.getId().toString());
        deletedTaskDTO.setTitle(task.getTitle());
        deletedTaskDTO.setDescription(task.getDescription());
        deletedTaskDTO.setAssignees(task.getAssignees());
        deletedTaskDTO.setStatus(task.getStatus().toString());

        taskRepository.delete(task);

        return ResponseEntity.ok(deletedTaskDTO);
    }

    @Transactional
    public TaskDTO updateTask(Integer taskId, Task task) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task " + taskId + " does not exist !!!!"));

        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setAssignees(task.getAssignees());
        existingTask.setStatus(task.getStatus());

        Task updatedTask = taskRepository.save(existingTask);

        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setId(String.valueOf(updatedTask.getId()));
        updatedTaskDTO.setTitle(updatedTask.getTitle());
        updatedTaskDTO.setDescription(updatedTask.getDescription());
        updatedTaskDTO.setAssignees(updatedTask.getAssignees());
        updatedTaskDTO.setStatus(updatedTask.getStatus().toString());

        return updatedTaskDTO;
    }

}
