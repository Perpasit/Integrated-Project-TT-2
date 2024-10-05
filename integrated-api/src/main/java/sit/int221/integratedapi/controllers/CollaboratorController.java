package sit.int221.integratedapi.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.exceptions.CustomAuthenticationException;
import sit.int221.integratedapi.exceptions.IllegalArgumentException;
import sit.int221.integratedapi.exceptions.ItemNotFoundException;
import sit.int221.integratedapi.services.*;
import sit.int221.integratedapi.utils.ListMapper;
import java.util.List;

@RestController
@RequestMapping("/v3/boards")
@CrossOrigin(origins = {"http://localhost:4173/", "http://localhost:5173/", "http://ip23tt2.sit.kmutt.ac.th", "http://intproj23.sit.kmutt.ac.th"})
public class CollaboratorController {
    @Autowired
    private BoardService boardService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ListMapper listMapper;

    @GetMapping("/{boardId}/collabs")
    public ResponseEntity<List<CollaboratorDTO>> getCollaboratorsByBoardId(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId) {

        String oidFromToken = null;

        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }

        List<CollaboratorDTO> collaborators = collaboratorService.getCollaboratorsByBoardId(boardId, oidFromToken);
        return ResponseEntity.ok(collaborators);
    }


    @GetMapping("/{boardId}/collabs/{collabOid}")
    public ResponseEntity<CollaboratorDTO> getCollaboratorById(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable String boardId,
            @PathVariable String collabOid) {

        String oidFromToken = null;

        if (token != null && !token.isEmpty()) {
            oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""), claims -> claims.get("oid", String.class));
        }

        boardService.validateBoardAccessForGet(boardId, oidFromToken);

        CollaboratorDTO collaborator = collaboratorService.getCollaboratorById(boardId, collabOid, oidFromToken);
        if (collaborator == null) {
            throw new ItemNotFoundException("Collaborator not found");
        }

        return ResponseEntity.ok(collaborator);
    }

    @PostMapping("/{boardId}/collabs")
    public ResponseEntity<CollaboratorDTO> addCollaborator(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable String boardId,
            @Valid @RequestBody CollaboratorCDTO collaboratorCDTO) {

        String oidFromToken = jwtTokenUtil.getClaimFromToken(token.replace("Bearer ", ""),
                claims -> claims.get("oid", String.class));

        CollaboratorDTO newCollaborator = collaboratorService.addCollaboratorByEmail(boardId, collaboratorCDTO.getUserEmail(), collaboratorCDTO.getAccessRight(), oidFromToken);

        return ResponseEntity.status(HttpStatus.CREATED).body(newCollaborator);
    }
}
