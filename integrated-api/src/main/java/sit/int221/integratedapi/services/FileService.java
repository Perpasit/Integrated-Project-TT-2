package sit.int221.integratedapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.entities.TaskAttachment;
import sit.int221.integratedapi.repositories.primary.TaskAttachmentRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
public class FileService {

    private final TaskAttachmentRepository taskAttachmentRepository;

    @Autowired
    public FileService(TaskAttachmentRepository attachmentRepository) {
        this.taskAttachmentRepository = attachmentRepository;
    }


    public String getFilePathById(Integer attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with ID: " + attachmentId));
        return attachment.getFilePath();
    }

    public String getFileNameById(Integer attachmentId) {
        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found with ID: " + attachmentId));
        return attachment.getFileName();
    }
}

