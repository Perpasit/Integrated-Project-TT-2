package sit.int221.intregratedapi.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDateTime;
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

    @Column(name = "statusName", nullable = false, unique = true)
    private String name;

    @Column(name = "statusDescription")
    private String description;

    @Column(name = "colorCode")
    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Invalid color code format")
    private String colorCode;

    @Column(name = "createdOn", nullable = false)
    private LocalDateTime createdOn;

    @Column(name = "updatedOn", nullable = false)
    private LocalDateTime updatedOn;

    @ManyToOne
    @JoinColumn(name = "global_settings_id", nullable = false)
    private GlobalSetting globalSetting;

    @OneToMany(mappedBy = "status", cascade = CascadeType.PERSIST)
    private List<Task> tasks = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }

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