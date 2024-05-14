package sit.int221.intregratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.dtos.TaskCDTO;
import sit.int221.intregratedapi.dtos.TaskDDTO;
import sit.int221.intregratedapi.dtos.TaskRDTO;
import sit.int221.intregratedapi.dtos.TaskUDTO;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;
import sit.int221.intregratedapi.utils.ListMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Task> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks;
    }


    @Transactional
    public ResponseEntity<Object> createNewTask(TaskCDTO taskCDTO) {
        Task task = modelMapper.map(taskCDTO, Task.class);

        if (taskCDTO.getStatus() != null) {
            Status status = statusRepository.findById(Integer.valueOf(taskCDTO.getStatus()))
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + taskCDTO.getStatus() + " does not exist!"));
            task.setStatus(status);
        }

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
    public ResponseEntity<TaskUDTO> updateTask(Integer taskId, Task task) {
        Task existingTask = getTaskById(taskId);
        setDefaultValuesForTask(task);
        updateTaskFromDTO(existingTask, task);
        Task updatedTask = taskRepository.save(existingTask);
        TaskUDTO taskUDTO = modelMapper.map(updatedTask, TaskUDTO.class);
        return ResponseEntity.ok(taskUDTO);
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

        // Set description to null if it is null or empty after trimming whitespace
        task.setDescription((task.getDescription() == null || task.getDescription().trim().isEmpty()) ? null : task.getDescription().trim());

        // Set assignees to null if it is null or empty after trimming whitespace
        task.setAssignees((task.getAssignees() == null || task.getAssignees().trim().isEmpty()) ? null : task.getAssignees().trim());


        if (task.getCreatedOn() == null) {
            task.setCreatedOn(LocalDateTime.now());
        }
        task.setUpdatedOn(LocalDateTime.now());
    }

    private void updateTaskFromDTO(Task existingTask, Task task) {
        existingTask.setTitle(task.getTitle());
        existingTask.setDescription(task.getDescription());
        existingTask.setAssignees(task.getAssignees());

        if (task.getStatus() != null && task.getStatus().getId() != null) {
            Status status = statusRepository.findById(task.getStatus().getId())
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + task.getStatus().getId() + " does not exist!"));
            existingTask.setStatus(status);
        }

        existingTask.setUpdatedOn(LocalDateTime.now());
    }


}