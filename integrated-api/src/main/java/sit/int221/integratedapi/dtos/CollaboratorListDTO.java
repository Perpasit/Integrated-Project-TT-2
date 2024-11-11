package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorListDTO {
    private List<CollaboratorDTO> collaboratorList;
    private List<InvitationBoardDTO> invitationList;
}
