package sit.int221.integratedapi.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAttachmentRDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String fileType;
}
