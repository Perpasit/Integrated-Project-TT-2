package sit.int221.intregratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.dtos.StatusCDTO;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.dtos.StatusRDTO;
import sit.int221.intregratedapi.utils.ListMapper;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatusService {
    private final StatusRepository statusRepository;
    private final ListMapper listMapper;

    @Autowired
    public StatusService(StatusRepository statusRepository, ListMapper listMapper) {
        this.statusRepository = statusRepository;
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

    public ResponseEntity<StatusCDTO> createStatus(Status status) {
        setDefaultValuesForStatus(status);
        Status savedStatus = statusRepository.save(status);
        return ResponseEntity.status(HttpStatus.CREATED).body(toStatusCDTO(savedStatus));
    }

    public Status updateStatus(Integer id, Status status) {
        Status existingStatus = statusRepository.findById(id).orElse(null);
        if (existingStatus != null) {
            existingStatus.setStatusName(status.getStatusName().trim());
            existingStatus.setStatusDescription(status.getStatusDescription() != null ? status.getStatusDescription().trim() : null);
            existingStatus.setColorCode(status.getColorCode());
            return statusRepository.save(existingStatus);
        }
        throw new ItemNotFoundException("Status " + id + " does not exist!");
    }

    public void deleteStatus(Integer id) {
        if (statusRepository.existsById(id)) {
            statusRepository.deleteById(id);
        } else {
            throw new ItemNotFoundException("Status " + id + " does not exist!");
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

    private void setDefaultValuesForStatus(Status status) {
        if (status.getColorCode() == null || status.getColorCode().isEmpty()) {
            status.setColorCode("#000000"); // Default color code is black
        }
        if (status.getCreatedOn() == null) {
            status.setCreatedOn(LocalDateTime.now());
        }
        if (status.getUpdatedOn() == null) {
            status.setUpdatedOn(LocalDateTime.now());
        }
    }
}