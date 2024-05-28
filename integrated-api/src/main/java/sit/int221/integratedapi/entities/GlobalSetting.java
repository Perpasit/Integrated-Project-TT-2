package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "global_settings")
public class GlobalSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "maximumTaskLimit", nullable = false)
    private Integer maximumTaskLimit;

    @Column(name = "limitMaximumTask", nullable = false)
    private boolean limitMaximumTask;
    public GlobalSetting(String name, Integer maximumTaskLimit, boolean limitMaximumTask) {
        this.name = name;
        this.maximumTaskLimit = maximumTaskLimit;
        this.limitMaximumTask = limitMaximumTask;
    }
}
