package sit.int221.integratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDDTO {
    private String inviterName;
    private String inviteeName;
    private String boardName;
    private String accessRight;
    private String status;
}
