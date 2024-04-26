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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "http://localhost:4173")
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
    public ResponseEntity<List<Task>> getTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok().body(tasks);
    }

//    @GetMapping("")
//    public ResponseEntity<List<TaskDTO>> getTasks() {
//        List<TaskDTO> taskDTOs = listMapper.mapList(taskService.getAllTasks(), TaskDTO.class);
//        return ResponseEntity.ok(taskDTOs);
//    }


    @GetMapping("/{taskId}")
    public ResponseEntity<?> getTaskById(@PathVariable Integer taskId) {
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            Map<String, Object> body = new LinkedHashMap<>();

            String formattedTimestamp = OffsetDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")).replace("Z","+00:00");
            body.put("timestamp", formattedTimestamp);
            body.put("status", 404);
            body.put("error", "Not Found");
            body.put("message", "Task " + taskId + " does not exist !!!");
            body.put("path", "/itb-kk/v1/tasks/"+taskId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        return ResponseEntity.ok(task);
    }

}
