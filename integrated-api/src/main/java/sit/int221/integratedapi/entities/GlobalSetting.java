package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "global_settings")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
