package sit.int221.intregratedapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer taskID;
    @Column(name = "task_Title", nullable = false, length = 100)
    private String taskTitle;
    @Column(name = "task_Description", nullable = false, length = 100)
    private String taskDescription;
    private String taskAssignees;
    private String taskStatus;
    @Column(name = "createdOn")
    private Date createdOn = new Date();
    @Column(name = "updatedOn")
    private Date updatedOn = new Date();
}

