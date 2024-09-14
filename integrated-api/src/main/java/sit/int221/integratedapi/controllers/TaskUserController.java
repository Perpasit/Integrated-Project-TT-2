package sit.int221.integratedapi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.services.TaskUserService;

import java.util.List;

@RestController
@RequestMapping("/taskusers")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class TaskUserController {

    @Autowired
    private TaskUserService taskUserService;

    @GetMapping("")
    public ResponseEntity<List<TaskUser>> getAllTaskUsers() {
        try {
            List<TaskUser> taskUsers = taskUserService.getAllTaskUsers();
            return ResponseEntity.ok(taskUsers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}
