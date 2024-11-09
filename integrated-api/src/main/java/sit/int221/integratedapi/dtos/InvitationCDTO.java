package sit.int221.integratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvitationCDTO {
    private String inviterName;
    private String inviteeName;
    private String inviteeEmail;
    private String boardName;
    private String accessRight;
    private String status;
    private String createdOn;
}
