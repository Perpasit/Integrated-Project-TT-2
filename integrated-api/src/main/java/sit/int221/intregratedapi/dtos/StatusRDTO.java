package sit.int221.intregratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class StatusRDTO {
    private Integer id;
    private String name;
    private String description;
    private String colorCode;
    private String createdOn;
    private String updatedOn;
}
