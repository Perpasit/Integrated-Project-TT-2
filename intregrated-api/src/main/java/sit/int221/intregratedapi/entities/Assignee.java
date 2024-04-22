package sit.int221.intregratedapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Assignee {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long assigneeId;
    private String assigneeName;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;
}
