package sit.int221.intregratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDTO {
    private String name;
    private String description;
    private String colorCode;
    private boolean limitMaximumTask;
    private Integer maximumTaskLimit;
}