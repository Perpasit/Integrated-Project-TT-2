package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Board name cannot be null or empty.")
    @Size(max = 45, message = "Board name exceeds maximum length of 45 characters.")
    private String name;

    @Column(name = "taskusers_oid", nullable = false, length = 255)
    private String taskusersOid;

    @OneToMany(mappedBy = "board")
    private Set<Task> tasks;


}
