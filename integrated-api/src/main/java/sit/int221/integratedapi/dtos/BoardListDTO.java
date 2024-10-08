package sit.int221.integratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardListDTO {
    private List<BoardDTO> personalBoards;
    private List<BoardDTO> collabBoards;
}
