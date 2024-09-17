package sit.int221.integratedapi.entities;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "taskusers")
public class TaskUser {
    @Id
    @Column(name = "oid", nullable = false, unique = true)
    private String oid;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    private String role;

    @Column(name = "token")
    private String token;

    @Column(name = "createdOn")
    private String createdOn;
}

