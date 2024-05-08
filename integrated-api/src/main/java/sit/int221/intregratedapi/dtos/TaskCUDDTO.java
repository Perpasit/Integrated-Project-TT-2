package sit.int221.intregratedapi.dtos;

import lombok.Data;
import sit.int221.intregratedapi.entities.TaskStatus;

@Data
public class TaskCUDDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private TaskStatus status;

    public void setDescription(String description) {
        this.description = description;
    }


    public void setStatus(String statusStr) {
        if (statusStr == null || statusStr.isEmpty()) {
            this.status = TaskStatus.NO_STATUS;
        } else {
            try {
                this.status = TaskStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Handle invalid status value
                this.status = TaskStatus.NO_STATUS;
            }
        }
    }
}
