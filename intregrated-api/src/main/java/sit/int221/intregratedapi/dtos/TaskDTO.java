package sit.int221.intregratedapi.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TaskDTO {
    private String title;
    private String assignees;
    private String status;
}
