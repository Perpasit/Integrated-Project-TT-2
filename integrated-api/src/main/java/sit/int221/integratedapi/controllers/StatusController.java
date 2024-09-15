package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.services.StatusService;
import sit.int221.integratedapi.utils.ListMapper;
import java.util.List;

@RestController
@RequestMapping("/v2/statuses")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/" , "http://ip23tt2.sit.kmutt.ac.th","http://intproj23.sit.kmutt.ac.th"})
public class StatusController {
    private final StatusService statusService;
    private final ListMapper listMapper;

    public StatusController(StatusService statusService, ListMapper listMapper) {
        this.statusService = statusService;
        this.listMapper = listMapper;
    }

//    @GetMapping("")
//    public ResponseEntity<List<StatusDTO>> getStatuses() {
//        List<StatusDTO> statusDTOs = statusService.getAllStatuses();
//        return ResponseEntity.ok(statusDTOs);
//    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusRDTO> getStatusById(@PathVariable Integer id) {
        StatusRDTO statusRDTO = statusService.getStatusById(id);
        return ResponseEntity.ok(statusRDTO);
    }

    @PostMapping("")
    public ResponseEntity<Object> addNewStatus(@Valid @RequestBody StatusCDTO status) {

        return statusService.createStatus(status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StatusDDTO> removeStatus(@PathVariable Integer id){
        return statusService.removeStatus(id);
    }

    @DeleteMapping("/{id}/{newId}")
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus( @PathVariable Integer id, @PathVariable Integer newId) {
        return statusService.deleteTransferStatus(id,newId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateStatus(@Valid @RequestBody StatusUDTO statusUDTO, @PathVariable Integer id) {
        return statusService.updateStatus(id, statusUDTO);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByStatusId(@PathVariable Integer id) {
        List<TaskDTO> taskDTOs = statusService.getTasksByStatusId(id);
        return ResponseEntity.ok(taskDTOs);
    }

}