package sit.int221.integratedapi.dtos;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusCDTO {
    private Integer id;
    @Size(max = 50, message = "Name size must be between 0 and 50 characters")
    private String name;
    @Size(max = 200, message = "Description size must be between 0 and 200 characters")
    private String description;
    private Integer boardId;
    private String colorCode;
}
