package sit.int221.intregratedapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.*;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.services.TaskService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/"})
public class TaskController {
    private final TaskService taskService;
    private final ListMapper listMapper;

    public TaskController(TaskService taskService, ListMapper listMapper) {
        this.taskService = taskService;
        this.listMapper = listMapper;
    }

    @GetMapping("")
    public ResponseEntity<List<TaskDTO>> getTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
        return ResponseEntity.ok(taskDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskRDTO> getTaskById(@PathVariable Integer id) {
        TaskRDTO taskRDTO = taskService.getTaskRDTOById(id);
        return ResponseEntity.ok(taskRDTO);
    }

    @PostMapping("")
    public ResponseEntity<TaskCDTO> addNewTask(@RequestBody Task task) {

        return taskService.createNewTask(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskDDTO> removeTask(@PathVariable Integer id) {
        return taskService.removeTask(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskUDTO> updateTask(@RequestBody Task task, @PathVariable Integer id) {
        return taskService.updateTask(id, task);
    }
}