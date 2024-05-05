package sit.int221.intregratedapi.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskDTO {
    private String id;
    private String title;
    private String description;
    private String assignees;
    private String status;

    public void setDescription(String description) {
        this.description = description;
    }
}