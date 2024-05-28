package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusMaximumTaskDTO {
    private Integer id;
    private String name;
    private boolean limitMaximumTask;
    private Integer maximumTaskLimit;
    private int noOfTasks;
}