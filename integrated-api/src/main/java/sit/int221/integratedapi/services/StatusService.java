package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.GlobalSetting;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.repositories.StatusRepository;
import sit.int221.integratedapi.repositories.TaskRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class StatusService {
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private GlobalSettingService globalSettingService;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper modelMapper;

    private static final int MAX_STATUS_NAME_LENGTH = 50;
    private static final int MAX_STATUS_DESCRIPTION_LENGTH = 200;

    public List<StatusDTO> getAllStatuses() {
        List<Status> statuses = statusRepository.findAll();
        List<StatusDTO> statusDTOs = new ArrayList<>();
        for (Status status : statuses) {
            StatusDTO statusDTO = modelMapper.map(status, StatusDTO.class);
            statusDTO.setNoOfTasks(getNoOfTasksByStatusId(status.getId()));
            statusDTOs.add(statusDTO);
        }
        return statusDTOs;
    }

    public int getNoOfTasksByStatusId(Integer statusId) {
        return taskRepository.countByStatusId(statusId);
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
            StatusRDTO statusRDTO = modelMapper.map(status, StatusRDTO.class);
            statusRDTO.setGlobalSettingId(status.getGlobalSetting().getId());
            statusRDTO.setNoOfTasks(getNoOfTasksByStatusId(id));
            return statusRDTO;
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    @Transactional
    public ResponseEntity<Object> createStatus(StatusCDTO statusCDTO) {
        ValidationErrorException validationError = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation Error");

        if (statusCDTO.getName() == null || statusCDTO.getName().trim().isEmpty()) {
            validationError.addError("name", "Name must not be null or empty");
        } else if (statusCDTO.getName().length() > MAX_STATUS_NAME_LENGTH) {
            validationError.addError("name", "Name size must be between 0 and " + MAX_STATUS_NAME_LENGTH + " characters");
        }

        if (statusCDTO.getDescription() != null && statusCDTO.getDescription().length() > MAX_STATUS_DESCRIPTION_LENGTH) {
            validationError.addError("description", "Description size must be between 0 and " + MAX_STATUS_DESCRIPTION_LENGTH + " characters");
        }

        if (!validationError.getErrors().isEmpty()) {
            throw validationError;
        }

        if (statusCDTO.getId() != null) {
            // Update an existing status
            Status existingStatus = statusRepository.findById(statusCDTO.getId())
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + statusCDTO.getId() + " does not exist!"));

            validateStatusName(statusCDTO.getName(), existingStatus.getId());
            validateStatusDescription(statusCDTO.getDescription(), MAX_STATUS_DESCRIPTION_LENGTH);

            existingStatus.setName(statusCDTO.getName().trim());
            existingStatus.setDescription(statusCDTO.getDescription() != null ? statusCDTO.getDescription().trim() : null);
            if (statusCDTO.getColorCode() != null) {
                existingStatus.setColorCode(statusCDTO.getColorCode());
            }

            Status updatedStatus = statusRepository.save(existingStatus);
            StatusCDTO updatedStatusCDTO = modelMapper.map(updatedStatus, StatusCDTO.class);
            return ResponseEntity.ok(updatedStatusCDTO);
        } else {
            // Create a new status
            validateStatusName(statusCDTO.getName(), null);
            validateStatusDescription(statusCDTO.getDescription(), MAX_STATUS_DESCRIPTION_LENGTH);

            Status status = modelMapper.map(statusCDTO, Status.class);
            status.setName(status.getName().trim());
            if (status.getDescription() != null) {
                status.setDescription(status.getDescription().trim());
            }
            status.setName(status.getName().trim());
            status.setDescription(status.getDescription() != null && !status.getDescription().trim().isEmpty() ? status.getDescription().trim() : null);
            setDefaultValuesForStatus(status, "General");
            Status createdStatus = statusRepository.save(status);
            StatusCDTO createdStatusCDTO = modelMapper.map(createdStatus, StatusCDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStatusCDTO);
        }
    }


    @Transactional
    public ResponseEntity<StatusDDTO> removeStatus(Integer id) {
        Status status = statusRepository.findById(id).orElse(null);
        if (status != null) {
            // Check if the status name is "No Status" or "Done"
            if ("No Status".equalsIgnoreCase(status.getName()) || "Done".equalsIgnoreCase(status.getName())) {
                throw new NotAllowedOperationException(status.getName() + " cannot be deleted.");
            }

            // Check if there are tasks associated with this status
            int taskCount = taskRepository.countByStatusId(id);
            if (taskCount > 0) {
                // Send to transfer if tasks are associated with this status
                throw new TransferTasksRequiredException("destination status for task transfer not specified.");
            }

            // Proceed with the deletion logic if no tasks are associated
            statusRepository.delete(status);
            StatusDDTO statusDDTO = ListMapper.mapObject(status, StatusDDTO.class);
            return ResponseEntity.ok(statusDDTO);
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    @Transactional
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(Integer id, Integer newId) {
        Status statusToDelete = statusRepository.findById(id)
                .orElseThrow(() -> new ValidationErrorException(HttpStatus.BAD_REQUEST, "Status " + id + " does not exist!"));

        validateStatusNotDeletable(statusToDelete);

        // Retrieve the new status to transfer tasks to
        Status newStatus = statusRepository.findById(newId)
                .orElseThrow(() -> new ValidationErrorException(HttpStatus.BAD_REQUEST, "the specified status for task transfer does not exist"));

        // Check if the source and destination status are the same
        if (statusToDelete.equals(newStatus)) {
            throw new InvalidTransferStatusException("Destination status for task transfer must be different from current status", "the specified status for task transfer does not exist");

        }

        // Check if there are tasks associated with the status to delete
        int deletingStatusTaskCount = taskRepository.countByStatusId(id);

        // Check if the destination status is transferrable
        if (deletingStatusTaskCount > 0) {
            // Transfer tasks to the new status before deleting
            List<Task> tasksToUpdate = taskRepository.findByStatusId(id);
            for (Task task : tasksToUpdate) {
                task.setStatus(newStatus);
            }

            // Check if the destination status is over the task limit
            validateStatusNotTransferrable(newStatus, deletingStatusTaskCount + taskRepository.countByStatusId(newId));
        }

        // Delete the status
        statusRepository.delete(statusToDelete);

        // Map the deleted status to DTO for response
        StatusTFDDTO statusTFDDTO = modelMapper.map(statusToDelete, StatusTFDDTO.class);
        return ResponseEntity.ok(statusTFDDTO);
    }



    @Transactional
    public ResponseEntity<Object> updateStatus(Integer id, StatusUDTO statusUDTO) {
        Status existingStatus = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist!"));

        ValidationErrorException validationError = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation Error");

        // Validate statusUDTO fields
        if (statusUDTO.getName() == null) {
            validationError.addError("name", "must not be null");
            throw validationError;
        } else if (statusUDTO.getName().trim().isEmpty()) {
            validationError.addError("name", "Name must not be empty");
        } else if (statusUDTO.getName().length() > MAX_STATUS_NAME_LENGTH) {
            validationError.addError("name", "Name size must be between 0 and " + MAX_STATUS_NAME_LENGTH + " characters");
        } else {
            Status existingStatusWithName = statusRepository.findByNameIgnoreCase(statusUDTO.getName().trim()).orElse(null);
            if (existingStatusWithName != null && !existingStatusWithName.getId().equals(id)) {
                validationError.addError("name", "must be unique");
                validationError.addError("message", "Status name '" + statusUDTO.getName().trim() + "' already exists");
            }
        }

        if (statusUDTO.getDescription() != null && statusUDTO.getDescription().length() > MAX_STATUS_DESCRIPTION_LENGTH) {
            validationError.addError("description", "Description size must be between 0 and " + MAX_STATUS_DESCRIPTION_LENGTH + " characters");
        }

        // Check if the status name is "No Status" or "Done"
        if ("No Status".equalsIgnoreCase(existingStatus.getName()) || "Done".equalsIgnoreCase(existingStatus.getName())) {
            throw new NotAllowedOperationException(existingStatus.getName() + " cannot be modified.");
        }

        if (!validationError.getErrors().isEmpty()) {
            throw validationError;
        }

        // Update status fields
        if (statusUDTO.getName() != null) {
            existingStatus.setName(statusUDTO.getName().trim());
        }
        existingStatus.setDescription(statusUDTO.getDescription() != null && !statusUDTO.getDescription().trim().isEmpty() ? statusUDTO.getDescription().trim() : null);
        if (statusUDTO.getColorCode() != null) {
            existingStatus.setColorCode(statusUDTO.getColorCode());
        }

        Status savedStatus = statusRepository.save(existingStatus);
        StatusUDTO responseStatusUDTO = modelMapper.map(savedStatus, StatusUDTO.class);
        return ResponseEntity.ok(responseStatusUDTO);
    }


    private void setDefaultValuesForStatus(Status status, String globalSettingName) {
        if (status.getColorCode() == null || status.getColorCode().isEmpty()) {
            status.setColorCode("#000000");
        }
        if (status.getCreatedOn() == null) {
            status.setCreatedOn(ZonedDateTime.now());
        }
        if (status.getUpdatedOn() == null) {
            status.setUpdatedOn(ZonedDateTime.now());
        }
        if (status.getDescription() == null || status.getDescription().isEmpty()) {
            status.setDescription(null);
        }

        GlobalSetting globalSetting = globalSettingService.getGlobalSettingByName(globalSettingName);
        status.setGlobalSetting(globalSetting);
    }

    private void validateStatusName(String statusName, Integer id) {
        if (statusName == null || statusName.trim().isEmpty()) {
            throw new IllegalArgumentException("Status name cannot be null or empty");
        }

        if (statusName.length() > MAX_STATUS_NAME_LENGTH) {
            throw new IllegalArgumentException("Status name must be between 0 and " + MAX_STATUS_NAME_LENGTH + " characters");
        }

        Status existingStatus = statusRepository.findByNameIgnoreCase(statusName.trim()).orElse(null);
        if (existingStatus != null && !existingStatus.getId().equals(id)) {
            throw new DuplicateStatusException("Status name '" + statusName.trim() + "' already exists");
        }
    }

    private void validateStatusDescription(String description, int maxLength) {
        if (description != null && description.length() > maxLength) {
            throw new IllegalArgumentException("Status description must be between 0 and " + maxLength + " characters");
        }
    }

    private void validateStatusNotDeletable(Status status) {
        if (status.getName().equalsIgnoreCase("No Status") || status.getName().equalsIgnoreCase("Done")) {
            throw new IllegalArgumentException("Status with name '" + status.getName() + "' cannot be deleted or transferred");
        }
    }

    private void validateStatusNotTransferrable(Status status, int taskCount) {
        if (status != null && status.getGlobalSetting().isLimitMaximumTask() && taskCount > status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new TransferTasksRequiredException("The destination status cannot be over the limit after transfer");
        }
    }

}