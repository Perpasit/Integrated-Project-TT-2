package sit.int221.intregratedapi.controllers;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.TaskDTO;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.services.TaskService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = {"http://localhost:4173/","http://localhost:5173/"})
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

//    @GetMapping("")
//    public ResponseEntity<List<Task>> getTasksDetail() {
//        List<Task> tasks = taskService.getAllTasks();
//        return ResponseEntity.ok().body(tasks);
//    }

    @GetMapping("")
    public ResponseEntity<List<TaskDTO>> getTasks() {
        List<TaskDTO> taskDTOs = listMapper.mapList(taskService.getAllTasks(), TaskDTO.class);
        return ResponseEntity.ok(taskDTOs);
    }


    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable Integer taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PostMapping("")
    public ResponseEntity<TaskDTO> addNewTask(@RequestBody Task task) {
        return taskService.createNewTask(task);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<TaskDTO> removeTask(@PathVariable Integer taskId) {
        return taskService.removeTask(taskId);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@RequestBody Task task, @PathVariable Integer taskId) {
        TaskDTO updatedTaskDTO = taskService.updateTask(taskId, task);
        return ResponseEntity.ok(updatedTaskDTO);
    }



}
