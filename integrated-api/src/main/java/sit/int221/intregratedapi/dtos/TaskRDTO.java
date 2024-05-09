package sit.int221.intregratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskRDTO {
    private String id;
    private StringBuilder title;
    private StringBuilder description;
    private StringBuilder assignees;
    private String status;
    private String createdOn;
    private String updatedOn;

    public void setTitle(String title) {
        this.title = (title == null) ? new StringBuilder() : new StringBuilder(title);
    }

    public void setAssignees(String assignees) {
        this.assignees = (assignees == null) ? new StringBuilder() : new StringBuilder(assignees);
    }

}
