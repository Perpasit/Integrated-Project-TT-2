package sit.int221.intregratedapi.services;

import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.repositories.StatusRepository;

import java.util.List;

@Service
public class StatusService {
    private final StatusRepository statusRepository;

    public StatusService(StatusRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    public Status getStatusById(Integer id) {
        return statusRepository.findById(id).orElse(null);
    }

    public Status createStatus(Status status) {
        return statusRepository.save(status);
    }

    public Status updateStatus(Integer id, Status status) {
        Status existingStatus = statusRepository.findById(id).orElse(null);
        if (existingStatus != null) {
            existingStatus.setStatusName(status.getStatusName());
            existingStatus.setStatusDescription(status.getStatusDescription());
            return statusRepository.save(existingStatus);
        }
        return null;
    }

    public void deleteStatus(Integer id) {
        statusRepository.deleteById(id);
    }
}