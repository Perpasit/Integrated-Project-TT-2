package sit.int221.integratedapi.entities;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    private String oid;
    private String username;
    private String password;
    private String name;
    private String email;
    private String role;
}
