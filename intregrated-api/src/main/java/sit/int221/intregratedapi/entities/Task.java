package sit.int221.intregratedapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

import java.time.LocalDateTime;



@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long taskID;
    private String taskTitle;
    private String taskDescription;
    private String taskStatus;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;

    @OneToMany(mappedBy = "task")
    private List<Assignee> assignees;
}

