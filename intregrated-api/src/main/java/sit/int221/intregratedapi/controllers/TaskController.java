package sit.int221.intregratedapi.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.TaskDTO;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.services.TaskService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;
    private final ModelMapper modelMapper;
    private final ListMapper listMapper;

    @Autowired
    public TaskController(TaskService taskService, ModelMapper modelMapper, ListMapper listMapper) {
        this.taskService = taskService;
        this.modelMapper = modelMapper;
        this.listMapper = listMapper;
    }

    @GetMapping("")
    public ResponseEntity<List<TaskDTO>> getTasks() {
        List<TaskDTO> taskDTOs = listMapper.mapList(taskService.getAllTasks(), TaskDTO.class);
        return ResponseEntity.ok(taskDTOs);
    }


    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer taskId) {
        Task task = taskService.getTaskById(taskId);
        return new ResponseEntity<>(task, task == null ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

}
