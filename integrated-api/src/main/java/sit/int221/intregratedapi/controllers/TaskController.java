package sit.int221.intregratedapi.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.TaskDTO;
import sit.int221.intregratedapi.dtos.TaskCUDDTO;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.services.TaskService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/"})
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
        List<Task> tasks = taskService.getAllTasks();
        List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
        return ResponseEntity.ok(taskDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Integer id) {
        Task task = taskService.getTaskById(id);
        TaskDTO taskDTO = modelMapper.map(task, TaskDTO.class);
        return ResponseEntity.ok(taskDTO);
    }

    @PostMapping("")
    public ResponseEntity<TaskCUDDTO> addNewTask(@RequestBody TaskCUDDTO taskCUDDTO) {
        Task task = modelMapper.map(taskCUDDTO, Task.class);
        return taskService.createNewTask(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskCUDDTO> removeTask(@PathVariable Integer id) {
        return taskService.removeTask(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskCUDDTO> updateTask(@RequestBody TaskCUDDTO taskCUDDTO, @PathVariable Integer id) {
        Task task = modelMapper.map(taskCUDDTO, Task.class);
        TaskCUDDTO updatedTaskDTO = taskService.updateTask(id, task);
        return ResponseEntity.ok(updatedTaskDTO);
    }
}