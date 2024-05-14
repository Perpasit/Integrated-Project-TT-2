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
            return toStatusRDTO(status);
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    @Transactional
    public ResponseEntity<StatusCDTO> createStatus(Status status) {
        trimStatus(status);
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
        if (id.equals(newId)) {
            throw new IllegalArgumentException("The status you are transferring to cannot be the status you are deleting!");
        }

        Status statusToDelete = statusRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Status " + id + " that you are about to delete does not exist!"));

        Status newStatus = statusRepository.findById(newId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + newId + "that you are about to transfer does not exist!"));

        List<Task> tasksToUpdate = taskRepository.findByStatusId(id);
        for (Task task : tasksToUpdate) {
            task.setStatus(newStatus);
        }

        statusRepository.delete(statusToDelete);

        return ResponseEntity.ok(toStatusTFDDTO(statusToDelete));
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
        Status existingStatus = statusRepository.findById(id).orElseThrow(() -> new ItemNotFoundException("Status " + id + " does not exist!"));
        trimStatus(status);
        existingStatus.setStatusName(status.getStatusName().trim());
        existingStatus.setStatusDescription(status.getStatusDescription() != null ? status.getStatusDescription().trim() : null);
        existingStatus.setColorCode(status.getColorCode());
        existingStatus.setUpdatedOn(LocalDateTime.now());
        Status savedStatus = statusRepository.save(existingStatus);
        return ResponseEntity.ok(toStatusUDTO(savedStatus));
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

    private void trimStatus(Status status) {
        if (status.getStatusName() != null) {
            status.setStatusName(status.getStatusName().trim());
        }

        if (status.getStatusDescription() != null) {
            status.setStatusDescription(status.getStatusDescription().trim());
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