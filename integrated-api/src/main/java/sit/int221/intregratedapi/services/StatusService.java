package sit.int221.intregratedapi.services;

import jakarta.transaction.Transactional;
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
    private  StatusRepository statusRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private  ListMapper listMapper;


    @Autowired
    public StatusService(StatusRepository statusRepository, ListMapper listMapper) {
        this.statusRepository = statusRepository;
        this.taskRepository = taskRepository;
        this.listMapper = listMapper;
    }

    public List<Status> getAllStatuses() {
        List<Status> statuses = statusRepository.findAll();
        System.out.println("Retrieved statuses: " + statuses);
        return statuses;
    }

    public StatusRDTO getStatusById(Integer id) {
        Status status = statusRepository.findById(id).orElse(null);
        if (status != null) {
            return toStatusRDTO(status);
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    @Transactional
    public ResponseEntity<StatusCDTO> createStatus(Status status) {
        setDefaultValuesForStatus(status);
        Status savedStatus = statusRepository.save(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(toStatusCDTO(savedStatus));
    }


    @Transactional
    public ResponseEntity<StatusDDTO> removeStatus(Integer id) {
        Status status = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist!"));
        statusRepository.delete(status);
        StatusDDTO statusDDTO = toStatusDDTO(status);
        return ResponseEntity.ok(statusDDTO);
    }

    @Transactional
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(Integer id, Integer newId) {
        // 1. Find the status with the given id
        Status statusToDelete = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist!"));

        // 2. Find the status with the given newId
        Status newStatus = statusRepository.findById(newId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + newId + " does not exist!"));

        // 3. Update the tasks that were using the deleted status to use the new status
        List<Task> tasksToUpdate = taskRepository.findByStatusId(id);
        for (Task task : tasksToUpdate) {
            task.setStatus(newStatus);
        }

        // 4. Delete the status with the given id
        statusRepository.delete(statusToDelete);

        // 5. Return the deleted status as a StatusTFDDTO
        return ResponseEntity.ok(toStatusTFDDTO(statusToDelete));
    }



    private void updateTasksToUseNewStatus(Integer deletedStatusId, Integer newStatusId) {
        // Find the tasks that are using the deleted status
        List<Task> tasksToUpdate = taskRepository.findByStatusId(deletedStatusId);

        // Find the new status object
        Status newStatus = statusRepository.findById(newStatusId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + newStatusId + " does not exist!"));

        // Update the tasks to use the new status
        for (Task task : tasksToUpdate) {
            task.setStatus(newStatus);
            taskRepository.save(task);
        }
    }

    private StatusTFDDTO toStatusTFDDTO(Status status) {
        return StatusTFDDTO.builder()
                .id(status.getId())
                .statusName(status.getStatusName())
                .statusDescription(status.getStatusDescription())
                .build();
    }

    @Transactional
    public ResponseEntity<StatusUDTO> updateStatus(Integer id, Status status) {
        Status existingStatus = statusRepository.findById(id).orElse(null);
        if (existingStatus != null) {
            existingStatus.setStatusName(status.getStatusName().trim());
            existingStatus.setStatusDescription(status.getStatusDescription() != null ? status.getStatusDescription().trim() : null);
            existingStatus.setColorCode(status.getColorCode());
            Status savedStatus = statusRepository.save(existingStatus);
            return ResponseEntity.ok(toStatusUDTO(savedStatus));
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
    }

    private StatusRDTO toStatusRDTO(Status status) {
        return StatusRDTO.builder()
                .id(status.getId())
                .statusName(status.getStatusName())
                .statusDescription(status.getStatusDescription())
                .colorCode(status.getColorCode())
                .createdOn(status.getCreatedOn().format(DateTimeFormatter.ISO_DATE_TIME))
                .updatedOn(status.getUpdatedOn().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    private StatusCDTO toStatusCDTO(Status status) {
        return StatusCDTO.builder()
                .id(status.getId())
                .statusName(status.getStatusName())
                .statusDescription(status.getStatusDescription())
                .build();
    }


    private StatusDDTO toStatusDDTO(Status status) {
        return StatusDDTO.builder()
                .id(status.getId())
                .statusName(status.getStatusName())
                .statusDescription(status.getStatusDescription())
                .build();
    }

    private StatusUDTO toStatusUDTO(Status status) {
        return StatusUDTO.builder()
                .id(status.getId())
                .statusName(status.getStatusName())
                .statusDescription(status.getStatusDescription())
                .build();
    }

}