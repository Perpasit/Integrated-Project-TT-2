package sit.int221.intregratedapi.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sit.int221.intregratedapi.entities.TaskStatus;

@Data
public class TaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private StatusDTO status; // Make sure this matches your mappings
}