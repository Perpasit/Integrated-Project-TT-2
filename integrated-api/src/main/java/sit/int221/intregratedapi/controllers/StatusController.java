package sit.int221.intregratedapi.controllers;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.intregratedapi.dtos.*;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.services.StatusService;
import sit.int221.intregratedapi.utils.ListMapper;
import sit.int221.intregratedapi.dtos.StatusUpdateDTO;
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

    @GetMapping("")
    public ResponseEntity<List<StatusDTO>> getStatuses() {
        List<Status> statuses = statusService.getAllStatuses();
        List<StatusDTO> statusDTOs = listMapper.mapList(statuses, StatusDTO.class);
        return ResponseEntity.ok(statusDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StatusRDTO> getStatusById(@PathVariable Integer id) {
        StatusRDTO statusRDTO = statusService.getStatusById(id);
        return ResponseEntity.ok(statusRDTO);
    }

    @PostMapping("")
    public ResponseEntity<Object> addNewStatus(@RequestBody StatusCDTO status) {

        return statusService.createStatus(status);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StatusDDTO> removeStatus(@PathVariable Integer id){
        return statusService.removeStatus(id);
    }

    @DeleteMapping("/{id}/{newId}")
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(@PathVariable Integer id, @PathVariable Integer newId) {
        return statusService.deleteTransferStatus(id,newId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StatusUDTO> updateStatus(@RequestBody StatusUpdateDTO statusUpdateDTO, @PathVariable Integer id) {
        Status status = new Status();
        status.setName(statusUpdateDTO.getName().trim());

        if (statusUpdateDTO.getDescription() != null) {
            status.setDescription(statusUpdateDTO.getDescription().trim());
        } else {
            status.setDescription(null);
        }

        if (statusUpdateDTO.getColorCode() != null) {
            status.setColorCode(statusUpdateDTO.getColorCode());
        }

        return statusService.updateStatus(id, status);
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByStatusId(@PathVariable Integer id) {
        List<TaskDTO> taskDTOs = statusService.getTasksByStatusId(id);
        return ResponseEntity.ok(taskDTOs);
    }

}