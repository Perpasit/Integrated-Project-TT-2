package sit.int221.intregratedapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taskId")
    private Integer id;

    @Column(name = "taskTitle", length = 100, nullable = false)
    private String taskTitle;

    @Column(name = "taskDescription", length = 500)
    private String taskDescription;

    @Column(name = "taskAssignees", length = 30)
    private String taskAssignees;

    @ManyToOne
    @JoinColumn(name = "statusId", foreignKey = @ForeignKey(name = "fk_status"))
    @JsonIgnore
    private Status status;

    @Column(name = "createdOn", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "updatedOn", nullable = false)
    private LocalDateTime updatedOn;

    @PrePersist
    protected void onCreate() {
        createdOn = LocalDateTime.now();
        updatedOn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedOn = LocalDateTime.now();
    }
}