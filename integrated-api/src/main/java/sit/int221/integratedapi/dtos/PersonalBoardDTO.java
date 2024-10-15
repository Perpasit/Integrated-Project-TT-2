package sit.int221.integratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonalBoardDTO {
    private String id;
    private String name;
    private String visibility;
    private OwnerDTO owner;
}
