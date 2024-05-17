package sit.int221.intregratedapi.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalSettingDTO {
    private Integer id;
    private String name;
    private Integer maximumTaskLimit;
    private boolean limitMaximumTask;
}
