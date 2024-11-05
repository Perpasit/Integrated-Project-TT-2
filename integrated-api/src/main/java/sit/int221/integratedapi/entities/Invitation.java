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
@Table(name = "invitations", schema = "integrateddatabase")
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "inviter_name", nullable = false)
    private String inviterName; // ชื่อผู้ที่เชิญ

    @Column(name = "invitee_name", nullable = false)
    private String inviteeName; // ชื่อผู้ที่ได้รับคำเชิญ

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail; // อีเมลของผู้ที่ได้รับคำเชิญ

    @Enumerated(EnumType.STRING)
    @Column(name = "access_right", nullable = false)
    private Collaborator.AccessRight accessRight;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @Column(name = "created_on", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "updated_on", nullable = false)
    private ZonedDateTime updatedOn;

    public enum Status {
        PENDING, ACCEPTED, DECLINED
    }

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
