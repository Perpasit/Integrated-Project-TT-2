package sit.int221.intregratedapi.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.dtos.*;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.repositories.TaskRepository;
import sit.int221.intregratedapi.utils.ListMapper;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatusService {
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    public StatusService(StatusRepository statusRepository, ListMapper listMapper) {
        this.statusRepository = statusRepository;
        this.taskRepository = taskRepository;
        this.listMapper = listMapper;
    }

    public List<Status> getAllStatuses() {
        List<Status> statuses = statusRepository.findAll();
        return statuses;
    }

    public List<TaskDTO> getTasksByStatusId(Integer statusId) {
        Status status = statusRepository.findById(statusId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + statusId + " does not exist!"));

        List<Task> tasks = taskRepository.findByStatusId(statusId);
        return listMapper.mapList(tasks, TaskDTO.class);
    }

    public StatusRDTO getStatusById(Integer id) {
        Status status = statusRepository.findById(id).orElse(null);
        if (status != null) {
            return modelMapper.map(status, StatusRDTO.class);
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    @Transactional
    public ResponseEntity<Object> createStatus(StatusCDTO statusCDTO) {
        if (statusCDTO.getId() != null) {
            // Update an existing status
            Status existingStatus = statusRepository.findById(statusCDTO.getId())
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + statusCDTO.getId() + " does not exist!"));

            existingStatus.setName(statusCDTO.getName().trim());
            existingStatus.setDescription(statusCDTO.getDescription() != null ? statusCDTO.getDescription().trim() : null);
            Status updatedStatus = statusRepository.save(existingStatus);
            StatusCDTO updatedStatusCDTO = modelMapper.map(updatedStatus, StatusCDTO.class);
            return ResponseEntity.ok(updatedStatusCDTO);
        } else {
            // Create a new status
            String trimmedStatusName = statusCDTO.getName().trim().toLowerCase();
            boolean isDuplicate = statusRepository.findAll().stream()
                    .anyMatch(existingStatus -> existingStatus.getName().trim().toLowerCase().equals(trimmedStatusName));

            if (isDuplicate) {
                return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Duplicate status name: " + statusCDTO.getName());
            }

            Status status = modelMapper.map(statusCDTO, Status.class);
            status.setName(status.getName().trim());
            if (status.getDescription() != null) {
                status.setDescription(status.getDescription().trim());
            }

            setDefaultValuesForStatus(status);
            Status createdStatus = statusRepository.save(status);

            StatusCDTO createdStatusCDTO = modelMapper.map(createdStatus, StatusCDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStatusCDTO);
        }
    }

    @Transactional
    public ResponseEntity<StatusDDTO> removeStatus(Integer id) {
        Status status = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist!"));

        if (status.getName().equalsIgnoreCase("No Status") || status.getName().equalsIgnoreCase("Done")) {
            throw new IllegalArgumentException("Status with name \"" + status.getName() + "\" cannot be deleted!");
        }

        statusRepository.delete(status);
        StatusDDTO statusDDTO = ListMapper.mapObject(status, StatusDDTO.class);
        return ResponseEntity.ok(statusDDTO);
    }
    @Transactional
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(Integer id, Integer newId) {
        Status statusToDelete = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " that you are about to delete does not exist!"));

        if (statusToDelete.getName().equalsIgnoreCase("No Status") || statusToDelete.getName().equalsIgnoreCase("Done")) {
            throw new IllegalArgumentException("Status with name \"" + statusToDelete.getName() + "\" cannot be deleted or transferred!");
        }

        Status newStatus = statusRepository.findById(newId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + newId + "that you are about to transfer does not exist!"));

        int newStatusTaskCount = taskRepository.countByStatusId(newId);
        int deletingStatusTaskCount = taskRepository.countByStatusId(id);

        if (newStatusTaskCount + deletingStatusTaskCount > newStatus.getMaximumTaskLimit()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new StatusTFDDTO(statusToDelete.getId(), statusToDelete.getName(), statusToDelete.getDescription()));
        }

        List<Task> tasksToUpdate = taskRepository.findByStatusId(id);
        for (Task task : tasksToUpdate) {
            task.setStatus(newStatus);
        }

        statusRepository.delete(statusToDelete);

        StatusTFDDTO statusTFDDTO = ListMapper.mapObject(statusToDelete, StatusTFDDTO.class);
        return ResponseEntity.ok(statusTFDDTO);
    }

    @Transactional
    public ResponseEntity<StatusUDTO> updateStatus(Integer id, Status status) {
        Status existingStatus = statusRepository.findById(id).orElse(null);
        if (existingStatus != null) {
            if (existingStatus.getName().equalsIgnoreCase("Done")) {
                throw new IllegalArgumentException("Status with name \"Done\" cannot be updated!");
            }
            if (existingStatus.getName().equalsIgnoreCase("No Status")) {
                throw new IllegalArgumentException("Status with name \"No Status\" cannot be updated!");
            }
            existingStatus.setName(status.getName().trim());
            existingStatus.setDescription(status.getDescription() != null ? status.getDescription().trim() : null);
            if (status.getColorCode() != null) {
                existingStatus.setColorCode(status.getColorCode());
            }
            Status savedStatus = statusRepository.save(existingStatus);
            StatusUDTO statusUDTO = modelMapper.map(savedStatus, StatusUDTO.class);
            return ResponseEntity.ok(statusUDTO);
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    private void setDefaultValuesForStatus(Status status) {
        if (status.getColorCode() == null || status.getColorCode().isEmpty()) {
            status.setColorCode("#000000");
        }
        if (status.getCreatedOn() == null) {
            status.setCreatedOn(LocalDateTime.now());
        }
        if (status.getUpdatedOn() == null) {
            status.setUpdatedOn(LocalDateTime.now());
        }
        if (status.getDescription() == null || status.getDescription().isEmpty()) {
            status.setDescription(null);
        }
    }

    @Transactional
    public ResponseEntity<StatusMaximumTaskDTO> updateStatusMaximumTask(Integer id, StatusUpdateDTO statusUpdateDTO) {
        Status status = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status with id " + id + " does not exist!"));

        if (status.getName().equalsIgnoreCase("No Status") || status.getName().equalsIgnoreCase("Done")) {
            throw new IllegalArgumentException("Status with name \"" + status.getName() + "\" cannot be updated!");
        }

        String trimmedName = statusUpdateDTO.getName().trim().toLowerCase();
        boolean isDuplicate = statusRepository.findAll().stream()
                .anyMatch(existingStatus -> existingStatus.getName().trim().toLowerCase().equals(trimmedName) && !existingStatus.getId().equals(id));

        if (isDuplicate) {
            int noOfTasks = taskRepository.countByStatusId(id);
            StatusMaximumTaskDTO responseDTO = ListMapper.mapObject(status, StatusMaximumTaskDTO.class);
            responseDTO.setNoOfTasks(noOfTasks);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(responseDTO);
        }

        status.setName(statusUpdateDTO.getName().trim());
        status.setLimitMaximumTask(statusUpdateDTO.isLimitMaximumTask());

        if (statusUpdateDTO.getMaximumTaskLimit() != null) {
            status.setMaximumTaskLimit(statusUpdateDTO.getMaximumTaskLimit());
        } else {
            status.setMaximumTaskLimit(null);
        }

        status.setUpdatedOn(LocalDateTime.now());
        Status updatedStatus = statusRepository.save(status);
        int noOfTasks = taskRepository.countByStatusId(id);

        StatusMaximumTaskDTO responseDTO = ListMapper.mapObject(updatedStatus, StatusMaximumTaskDTO.class);
        responseDTO.setNoOfTasks(noOfTasks);
        return ResponseEntity.ok(responseDTO);
    }

}
