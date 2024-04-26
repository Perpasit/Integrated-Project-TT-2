package sit.int221.intregratedapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "tasks")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @JsonIgnore
    private Integer taskId;
    @Column(name = "taskTitle", length = 100, nullable = false)
    private String title;
    @Column(name = "taskDescription", length = 500)
    private String description;
    @Column(name = "taskAssignees", length = 30)
    private String assignees;

    @Enumerated(EnumType.STRING)
    @Column(name = "taskStatus")
    private TaskStatus status;

    @Column(name = "createdOn")
    private Date createdOn = new Date();
    @Column(name = "updatedOn")
    private Date updatedOn = new Date();
}

