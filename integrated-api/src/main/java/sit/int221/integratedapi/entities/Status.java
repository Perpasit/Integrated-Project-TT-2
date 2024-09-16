package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "statuses")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusId")
    private Integer id;

    @Column(name = "statusName", nullable = false)
    private String name;

    @Column(name = "statusDescription")
    private String description;

    @Column(name = "colorCode")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color code format")
    private String colorCode;

    @Column(name = "createdOn", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "updatedOn", nullable = false)
    private ZonedDateTime updatedOn;

    @ManyToOne
    @JoinColumn(name = "global_settings_id", nullable = false)
    private GlobalSetting globalSetting;

    @ManyToOne
    @JoinColumn(name = "board_id", foreignKey = @ForeignKey(name = "fk_tasks_boards1"))
    private Board board;

    @OneToMany(mappedBy = "status", cascade = CascadeType.PERSIST)
    private List<Task> tasks = new ArrayList<>();

    @Override
    public String toString() {
        return name;
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