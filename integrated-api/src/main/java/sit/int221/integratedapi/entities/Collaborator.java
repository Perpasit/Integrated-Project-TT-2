package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "collaborators", schema = "integrateddatabase")
public class Collaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "access_right", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccessRight accessRight = AccessRight.READ;

    @Column(name = "added_on", nullable = false)
    private ZonedDateTime addedOn;

    @Column(name = "updated_on", nullable = false)
    private ZonedDateTime updateOn;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private TaskUser user;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    public enum AccessRight {
        READ, WRITE
    }

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
