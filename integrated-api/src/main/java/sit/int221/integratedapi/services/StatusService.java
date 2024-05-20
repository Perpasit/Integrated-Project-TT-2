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
import sit.int221.integratedapi.exceptions.CustomErrorResponse;
import sit.int221.integratedapi.exceptions.StatusLimitReachedException;
import sit.int221.integratedapi.repositories.StatusRepository;
import sit.int221.integratedapi.repositories.TaskRepository;
import sit.int221.integratedapi.utils.ListMapper;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.exceptions.DuplicateStatusException;

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

            setDefaultValuesForStatus(status, "General");
            Status createdStatus = statusRepository.save(status);
            StatusCDTO createdStatusCDTO = modelMapper.map(createdStatus, StatusCDTO.class);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStatusCDTO);
        }
    }

    @Transactional
    public ResponseEntity<StatusDDTO> removeStatus(Integer id) {
        Status status = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist!"));

        validateStatusNotDeletable(status);

        if (taskRepository.countByStatusId(id) > 0) {
            throw new IllegalArgumentException("Cannot delete a status that still has tasks");
        }

        statusRepository.delete(status);
        StatusDDTO statusDDTO = ListMapper.mapObject(status, StatusDDTO.class);
        return ResponseEntity.ok(statusDDTO);
    }

    @Transactional
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(Integer id, Integer newId) {
        Status statusToDelete = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " that you are about to delete does not exist!"));

        validateStatusNotDeletable(statusToDelete);

        Status newStatus = statusRepository.findById(newId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + newId + " that you are about to transfer does not exist!"));

        if (id.equals(newId)) {
            throw new IllegalArgumentException("Source and destination status cannot be the same");
        }

        int deletingStatusTaskCount = taskRepository.countByStatusId(id);
        validateStatusNotTransferrable(newStatus, deletingStatusTaskCount + taskRepository.countByStatusId(newId));

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
            validateStatusNotDeletable(existingStatus);

            validateStatusName(status.getName(), id);
            validateStatusDescription(status.getDescription(), MAX_STATUS_DESCRIPTION_LENGTH);

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
        if (status.getGlobalSetting().isLimitMaximumTask() && taskCount > status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The destination status cannot be over the limit after transfer");
        }
    }

    private CustomErrorResponse validateFieldSize(String field, String value, int maxSize, String path) {
        if (value != null && value.length() > maxSize) {
            return new CustomErrorResponse(
                    LocalDateTime.now(),
                    HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(),
                    "The field '" + field + "' must be between 0 and " + maxSize + " characters",
                    path
            );
        }
        return null;
    }
}
