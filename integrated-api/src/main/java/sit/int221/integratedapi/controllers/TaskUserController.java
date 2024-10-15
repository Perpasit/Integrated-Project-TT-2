//package sit.int221.integratedapi.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import sit.int221.integratedapi.entities.User;
//import sit.int221.integratedapi.services.UserService;
//import sit.int221.integratedapi.services.UserService;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/taskusers")
//@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
//public class TaskUserController {
//
//    @Autowired
//    private UserService userService;
//
//    @GetMapping("")
//    public ResponseEntity<List<User>> getAllTaskUsers() {
//        try {
//            List<User> taskUsers = userService.getAllTaskUsers();
//            return ResponseEntity.ok(taskUsers);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(null);
//        }
//    }
//}
