package sit.int221.integratedapi.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileDownloadService {
    public Resource downloadFile(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();

            if (!Files.exists(path) || !Files.isReadable(path)) {
                throw new RuntimeException("File not found or not readable at path: " + path);
            }

            return new UrlResource(path.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error creating UrlResource for file: " + filePath, e);
        }
    }

    public String determineMimeType(String filePath) {
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            String mimeType = Files.probeContentType(path);
            return (mimeType != null) ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }
}

