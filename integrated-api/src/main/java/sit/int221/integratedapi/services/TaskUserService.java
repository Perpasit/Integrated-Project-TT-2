package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;

import java.util.List;

@Service
public class TaskUserService {

    @Autowired
    private TaskUserRepository taskUserRepository;

    public List<TaskUser> getAllTaskUsers() {
        return taskUserRepository.findAll();
    }

    @Transactional
    public void saveOrUpdateTaskUser(TaskUser taskUser) {
        // Find existing user by OID
        TaskUser existingUser = taskUserRepository.findByOid(taskUser.getOid());
        if (existingUser != null) {
            // Update existing user
            existingUser.setUsername(taskUser.getUsername());
            taskUserRepository.save(existingUser);
        } else {
            // Insert new user
            taskUserRepository.save(taskUser);
        }
    }
}
