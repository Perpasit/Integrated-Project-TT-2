package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class StatusUDTO {
    private Integer id;
    private String name;
    private String description;
    private String colorCode;
    private String createdOn;
    private String updatedOn;
}
