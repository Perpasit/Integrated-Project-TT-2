package sit.int221.integratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int221.integratedapi.entities.Collaborator;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorDTO {
    private String oid;
    private String name;
    private String email;
    private Collaborator.AccessRight accessRight;
    private Collaborator.Status status;
    private String addedOn;
}

