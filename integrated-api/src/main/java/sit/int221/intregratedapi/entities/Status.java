package sit.int221.intregratedapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "statuses")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusId")
    private Integer id;

    @Column(name = "statusName", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "statusDescription", length = 200)
    private String description;


}