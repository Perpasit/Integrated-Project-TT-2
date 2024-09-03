package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
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

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @Column(name = "users_oid", nullable = false, length = 255)
    private String usersOid;

    @OneToMany(mappedBy = "board")
    private Set<Task> tasks;


}
