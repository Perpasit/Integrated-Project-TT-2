package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.*;


import java.time.ZonedDateTime;

@Entity
@Table(name = "tasks", schema = "integrateddatabase")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "assignees", length = 30)
    private String assignees;

    @ManyToOne
    @JoinColumn(name = "status_id", foreignKey = @ForeignKey(name = "fk_tasks_statuses"))
    private Status status;

    @Column(name = "created_on", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "updated_on", nullable = false)
    private ZonedDateTime updatedOn;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tasks_boards"))
    private Board board;

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
