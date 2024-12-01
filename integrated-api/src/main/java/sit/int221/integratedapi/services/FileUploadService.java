package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;


@Service
public class FileUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file, String boardId, Integer taskId) throws IOException {
        String directoryPath = uploadDir + "/" + boardId + "/tasks/" + taskId;
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

        String filePath = directoryPath + "/" + uniqueFileName;
        Path path = Paths.get(filePath);

        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        return filePath;
    }


    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        } else {
            throw new IOException("File not found: " + filePath);
        }
    }
}






