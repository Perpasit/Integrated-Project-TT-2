package sit.int221.integratedapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.services.TaskService;
import sit.int221.integratedapi.utils.ListMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            @RequestParam(required = false) List<String> filterStatuses,
            @RequestParam(required = false) List<String> allParams) {

        // ตรวจสอบว่า filterStatuses มีค่าและมี # หรือไม่
        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>()); // ส่งคืน []
        }

        // ตรวจสอบความถูกต้องของ sortBy
        if (sortBy != null && !isValidSortBy(sortBy)) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // ตรวจสอบว่ามีอาร์กิวเมนต์ที่ไม่คาดหวังหรือไม่
        if (allParams != null && !allParams.isEmpty()) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        ResponseEntity<Object> response = taskService.getAllTasks(sortBy, filterStatuses, allParams);
        if (response.getStatusCode().is2xxSuccessful()) {
            List<Task> tasks = (List<Task>) response.getBody();
            List<TaskDTO> taskDTOs = listMapper.mapList(tasks, TaskDTO.class);
            return ResponseEntity.ok(taskDTOs);
        } else {
            return response;
        }
    }
    private boolean isValidSortBy(String sortBy) {
        return sortBy.equalsIgnoreCase("asc") || sortBy.equalsIgnoreCase("desc") || sortBy.equalsIgnoreCase("createOn");
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