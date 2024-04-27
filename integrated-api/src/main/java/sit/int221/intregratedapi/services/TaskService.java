package sit.int221.intregratedapi.services;


import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.exceptions.ItemNotFoundException;
import sit.int221.intregratedapi.repositories.TaskRepository;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public TaskService(TaskRepository taskRepository, ModelMapper modelMapper) {
        this.taskRepository = taskRepository;
        this.modelMapper = modelMapper;
    }

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id).orElseThrow(
                () -> new ItemNotFoundException(
                        "Task " + id + " does not exist !!!!")
        );

    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }


}
