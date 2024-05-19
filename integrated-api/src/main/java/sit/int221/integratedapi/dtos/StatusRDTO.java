package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusRDTO {
    private Integer id;
    private String name;
    private String description;
    private String colorCode;
    private String createdOn;
    private String updatedOn;
    private Integer globalSettingId;
    private int noOfTasks;
}