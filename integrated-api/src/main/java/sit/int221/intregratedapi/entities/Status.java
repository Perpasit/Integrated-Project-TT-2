package sit.int221.intregratedapi.entities;

import jakarta.persistence.*;
import lombok.*;

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
    @ToString.Exclude
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "statusId")
    private Integer id;

    @ToString.Exclude
    @Column(name = "statusName", nullable = false, unique = true)
    private String statusName;

    @ToString.Exclude
    @Column(name = "statusDescription")
    private String statusDescription;

    @ToString.Exclude
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @Override
    public String toString() {
        return statusName;
    }
}