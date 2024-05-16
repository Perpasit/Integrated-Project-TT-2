package sit.int221.intregratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusMaximumTaskDTO {
    private Integer id;
    private String name;
    private boolean limitMaximumTask;
    private Integer maximumTaskLimit;
    private int noOfTasks;
}