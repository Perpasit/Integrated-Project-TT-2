package sit.int221.intregratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.repositories.StatusRepository;
import sit.int221.intregratedapi.utils.ListMapper;

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