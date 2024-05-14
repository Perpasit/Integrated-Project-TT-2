package sit.int221.intregratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDDTO {
    private String id;
    private String title;
    private String assignees;
    private String status;
}
