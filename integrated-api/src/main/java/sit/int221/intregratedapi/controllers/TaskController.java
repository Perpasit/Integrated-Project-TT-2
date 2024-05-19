package sit.int221.intregratedapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.*;
import sit.int221.intregratedapi.entities.Task;
import sit.int221.intregratedapi.services.TaskService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/v2/tasks")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class TaskController {
    private final TaskService taskService;
    private final ListMapper listMapper;

    public TaskController(TaskService taskService, ListMapper listMapper) {
        this.taskService = taskService;
        this.listMapper = listMapper;
    }

//    @GetMapping("")
//    public ResponseEntity<List<TaskDTO>> getTasks() {
//        List<Task> tasks = taskService.getAllTasks();
//        List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
//        return ResponseEntity.ok(taskDTOs);
//    }

    @GetMapping("")
    public ResponseEntity<Object> getTasks(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) List<String> filterStatuses
    ) {
        if (sortBy != null && !sortBy.equalsIgnoreCase("asc") && !sortBy.equalsIgnoreCase("desc")) {
            return ResponseEntity.badRequest().body("Invalid sort field. Only 'asc' and 'desc' are allowed.");
        }

        List<Task> tasks = taskService.getAllTasks(sortBy, filterStatuses);
        List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
        return ResponseEntity.ok(taskDTOs);
    }


    @GetMapping("/{id}")
    public ResponseEntity<TaskRDTO> getTaskById(@PathVariable Integer id) {
        TaskRDTO taskRDTO = taskService.getTaskDetailsById(id);
        return ResponseEntity.ok(taskRDTO);
    }

    @PostMapping("")
    public ResponseEntity<Object> addNewTask(@RequestBody TaskCDTO task) {
        return taskService.createNewTask(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TaskDDTO> removeTask(@PathVariable Integer id) {
        return taskService.removeTask(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateTask(@RequestBody TaskUDTO updatedTaskDTO, @PathVariable Integer id) {
        return taskService.updateTask(id, updatedTaskDTO);
    }
}