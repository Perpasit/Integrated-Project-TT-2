package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.services.JwtTokenUtil;
import sit.int221.integratedapi.services.StatusService;
import sit.int221.integratedapi.utils.ListMapper;
import java.util.List;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class StatusController {
    private final StatusService statusService;
    private final ListMapper listMapper;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    public StatusController(StatusService statusService, ListMapper listMapper) {
        this.statusService = statusService;
        this.listMapper = listMapper;
    }
    @GetMapping("/{boardId}/statuses")
    public ResponseEntity<List<StatusDTO>> getStatuses(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }
        statusService.validateBoardAccessForGet(boardId, oidFromToken);
        List<StatusDTO> statusDTOs = statusService.getStatusesByBoardId(boardId);
        return ResponseEntity.ok(statusDTOs);
    }


    @GetMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<StatusRDTO> getStatusById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer statusId) {

        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }
        statusService.validateBoardAccessForGet(boardId, oidFromToken);
        StatusRDTO statusRDTO = statusService.getStatusByBoardIdAndStatusId(boardId, statusId);
        return ResponseEntity.ok(statusRDTO);
    }


    @PostMapping("/{boardId}/statuses")
    public ResponseEntity<Object> addNewStatus(@RequestHeader(value = "Authorization",required = false) String token,
                                               @PathVariable String boardId,
                                               @Valid @RequestBody(required = false) StatusCDTO status) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        // statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        statusService.validateWriteAccess(boardId, oidFromToken);
        if (status == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }
        return statusService.createStatus(boardId, status);
    }

    @GetMapping("/{boardId}/statuses/{statusId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByBoardAndStatusId(
            @RequestHeader(value = "Authorization",required = false) String token,
            @PathVariable String boardId,
            @PathVariable Integer statusId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }
        statusService.validateBoardAccessForGet(boardId,oidFromToken);
        List<TaskDTO> taskDTOs = statusService.getTasksByBoardAndStatusId(boardId, statusId);
        return ResponseEntity.ok(taskDTOs);
    }

    @PutMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<Object> updateStatus(
            @RequestHeader(value = "Authorization",required = false) String token,
            @Valid @RequestBody(required = false) StatusUDTO statusUDTO,
            @PathVariable String boardId,
            @PathVariable Integer statusId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        // statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        statusService.validateWriteAccess(boardId, oidFromToken);
        statusService.findStatusByIdAndBoardId(statusId,boardId);
        if (statusUDTO == null) {
            throw new IllegalArgumentException("Request body is missing or invalid. Please check the provided data and try again.");
        }
        return statusService.updateStatus(boardId, statusId, statusUDTO);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}")
    public ResponseEntity<StatusDDTO> removeStatus(@RequestHeader(value = "Authorization",required = false) String token,
                                                   @PathVariable String boardId,
                                                   @PathVariable Integer statusId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        // statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        statusService.validateWriteAccess(boardId, oidFromToken);
        statusService.findStatusByIdAndBoardId(statusId,boardId);
        return statusService.removeStatus(boardId, statusId);
    }

    @DeleteMapping("/{boardId}/statuses/{statusId}/{newId}")
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(@RequestHeader(value = "Authorization",required = false) String token,
                                                             @PathVariable String boardId,
                                                             @PathVariable Integer statusId,
                                                             @PathVariable Integer newId) {
        String oidFromToken = null;
        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        } else {
            throw new CustomAuthenticationException("You do not have permission to access this resource.");
        }
        // statusService.validateBoardOwnershipForModification(boardId,oidFromToken);
        statusService.validateWriteAccess(boardId, oidFromToken);
        statusService.findStatusByIdAndBoardId(statusId,boardId);
        return statusService.deleteTransferStatus(boardId, statusId, newId);
    }

}