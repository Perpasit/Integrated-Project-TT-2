package sit.int221.integratedapi.services;

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
    private String uploadDir;  // This will be injected from application.properties

    public String saveFile(MultipartFile file) {
        // Ensure the directory exists
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();  // Create the directory if it doesn't exist
        }

        // Define file path and save the file (local or cloud storage)
        String filePath = uploadDir + "/" + file.getOriginalFilename();  // Use the upload directory path
        try {
            Path path = Paths.get(filePath);  // java.nio.file.Path for file handling
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
        return filePath;
    }
}



