package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int221.integratedapi.entities.Collaborator;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollaboratorCDTO {
    private String email;
    private String accessRight;
}
