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
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;
import sit.int221.intregratedapi.utils.ListMapper;

import java.time.LocalDateTime;
import java.util.List;

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

//    public List<Task> getAllTasks() {
//        List<Task> tasks = taskRepository.findAll();
//        return tasks;
//    }

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
        Task task = modelMapper.map(taskCDTO, Task.class);

        if (taskCDTO.getStatus() != null) {
            Status status = statusRepository.findById(Integer.valueOf(taskCDTO.getStatus()))
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + taskCDTO.getStatus() + " does not exist!"));


            if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("The status " + status.getName() + " has reached the maximum task limit.");
            }

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
    public ResponseEntity<Object> updateTask(Integer taskId, TaskUDTO updatedTaskDTO) {
        Task existingTask = getTaskById(taskId);
        setDefaultValuesForTask(existingTask);

        if (updatedTaskDTO.getStatus() != null) {
            Status status = statusRepository.findById(Integer.valueOf(updatedTaskDTO.getStatus()))
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + updatedTaskDTO.getStatus() + " does not exist!"));

            if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("The status " + status.getName() + " has reached the maximum task limit.");
            }

            existingTask.setStatus(status);
        }

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