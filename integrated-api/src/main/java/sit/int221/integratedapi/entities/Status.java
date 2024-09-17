package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;


import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "statuses", schema = "integrateddatabase")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "color", nullable = false)
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color code format")
    private String colorCode;

    @Column(name = "created_on", nullable = false)
    private ZonedDateTime createdOn;

    @Column(name = "updated_on", nullable = false)
    private ZonedDateTime updatedOn;

    @ManyToOne
    @JoinColumn(name = "global_settings_id", nullable = false)
    private GlobalSetting globalSetting;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = false, foreignKey = @ForeignKey(name = "fk_statuses_boards"))
    private Board board;

    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, orphanRemoval = true)
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
