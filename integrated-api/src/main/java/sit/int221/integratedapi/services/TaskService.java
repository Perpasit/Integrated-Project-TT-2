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
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.repositories.primary.StatusRepository;
import sit.int221.integratedapi.repositories.primary.TaskRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public TaskRDTO getTaskDetailsById(Integer id) {
        Task task = getTaskById(id);
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

        Task task = new Task();
        task.setTitle(taskCDTO.getTitle().trim());
        task.setStatus(status);
        task.setDescription(taskCDTO.getDescription() != null && !taskCDTO.getDescription().trim().isEmpty() ? taskCDTO.getDescription().trim() : null);
        task.setAssignees(taskCDTO.getAssignees() != null && !taskCDTO.getAssignees().trim().isEmpty() ? taskCDTO.getAssignees().trim() : null);
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

        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        if (updatedTaskDTO.getTitle() == null || updatedTaskDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null");
        }


        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        Status status = getDefaultStatus();
        if (updatedTaskDTO.getStatus() != null && !updatedTaskDTO.getStatus().isEmpty()) {
            try {
                status = statusRepository.findById(Integer.valueOf(updatedTaskDTO.getStatus()))
                        .orElseThrow(() -> new ItemNotFoundException("Status with id " + updatedTaskDTO.getStatus() + " does not exist!"));
            } catch (NoSuchElementException e) {
                validationErrors.addError("status", "does not exist");
                throw validationErrors;
            }
        }

        if (status.getGlobalSetting().isLimitMaximumTask() && taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + status.getName() + " will have too many tasks. Please make progress and update the status of existing tasks first.");
        }

        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        setDefaultValuesForTask(existingTask);
        existingTask.setStatus(status);
        existingTask.setTitle(updatedTaskDTO.getTitle().trim());
        existingTask.setDescription(updatedTaskDTO.getDescription() != null && !updatedTaskDTO.getDescription().trim().isEmpty() ? updatedTaskDTO.getDescription().trim() : null);
        existingTask.setAssignees(updatedTaskDTO.getAssignees() != null && !updatedTaskDTO.getAssignees().trim().isEmpty() ? updatedTaskDTO.getAssignees().trim() : null);
        existingTask.setUpdatedOn(ZonedDateTime.now());

        Task updatedTask = taskRepository.save(existingTask);
        TaskUDTO updatedTaskCDTO = modelMapper.map(updatedTask, TaskUDTO.class);
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
}