package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int221.integratedapi.entities.Board;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardCDTO {
    private String id;
    private String name;
    private Board.Visibility visibility;
    private OwnerDTO owner;
}
