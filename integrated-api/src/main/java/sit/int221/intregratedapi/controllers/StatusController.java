package sit.int221.intregratedapi.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sit.int221.intregratedapi.dtos.StatusDTO;
import sit.int221.intregratedapi.entities.Status;
import sit.int221.intregratedapi.services.StatusService;
import sit.int221.intregratedapi.utils.ListMapper;

import java.util.List;

@RestController
@RequestMapping("/statuses")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/"})
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
}