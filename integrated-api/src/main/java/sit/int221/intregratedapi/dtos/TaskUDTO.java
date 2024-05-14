package sit.int221.intregratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TaskUDTO {
    private String title;
    private String description;
    private String assignees;
    private String status;
}
