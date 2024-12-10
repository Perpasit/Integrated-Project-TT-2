package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task_attachments", schema = "integrateddatabase")
public class TaskAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_type", nullable = false, length = 255)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "added_on", nullable = false)
    private ZonedDateTime addedOn;

    @Column(name = "update_on", nullable = false)
    private ZonedDateTime updateOn;

    @PrePersist
    protected void onCreate() {
        addedOn = ZonedDateTime.now();
        updateOn = addedOn;
    }

    @PreUpdate
    protected void onUpdate() {
        updateOn = ZonedDateTime.now();
    }
}

