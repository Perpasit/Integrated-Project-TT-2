package sit.int221.intregratedapi.dtos;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusTFDDTO {
    private Integer id;

    @JsonProperty("name")
    private String statusName;

    @JsonProperty("description")
    private String statusDescription;
}
