package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.time.ZonedDateTime;
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
    private String title;

    @Column(name = "taskDescription", length = 500)
    private String description;

    @Column(name = "taskAssignees", length = 30)
    private String assignees;

    @ManyToOne
    @JoinColumn(name = "statusId", foreignKey = @ForeignKey(name = "fk_status"))
    private Status status;

    @Column(name = "createdOn", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "updatedOn", nullable = false)
    private ZonedDateTime updatedOn;

    @PrePersist
    protected void onCreate() {
        createdOn = ZonedDateTime.now();
        updatedOn = createdOn;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedOn = ZonedDateTime.now();
    }
}