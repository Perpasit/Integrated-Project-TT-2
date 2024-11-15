package sit.int221.integratedapi.services;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;  // Import the correct Path class for file handling
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

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

        // Define file path and save the file
        String filePath = directoryPath + "/" + file.getOriginalFilename();
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

    public String createThumbnail(String originalFilePath, String boardId, Integer taskId) throws IOException {
        String thumbnailDir = uploadDir + "/" + boardId + "/tasks/" + taskId + "/thumbnails";
        File dir = new File(thumbnailDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String thumbnailFilePath = thumbnailDir + "/thumb_" + Paths.get(originalFilePath).getFileName().toString();

        Thumbnails.of(new File(originalFilePath))
                .size(150, 150)
                .toFile(new File(thumbnailFilePath));

        return thumbnailFilePath;
    }

    public String getThumbnailPath(String directoryPath, String originalFileName) {
        return directoryPath + "/thumbnails/thumb_" + originalFileName;
    }

}





