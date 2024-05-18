package sit.int221.intregratedapi.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class StatusDTO {
    private Integer id;
    private String name;
    private String description;
    private String colorCode;
    private int noOfTasks;
}