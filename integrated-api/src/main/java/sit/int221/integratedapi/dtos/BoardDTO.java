package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardDTO {
    private Integer boardId;
    private String name;
    private OwnerDTO owner;
}
