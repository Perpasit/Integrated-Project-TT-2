package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class TaskDTO {
    private Integer id;
    private String title;
    private String assignees;
    private String status;
    private String boardName;
}