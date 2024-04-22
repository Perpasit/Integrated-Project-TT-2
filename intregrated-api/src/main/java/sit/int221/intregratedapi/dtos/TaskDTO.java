package sit.int221.intregratedapi.dtos;

import sit.int221.intregratedapi.entities.Assignee;
import java.time.LocalDateTime;
import java.util.List;

public class TaskDTO {
    private String taskTitle;
    private String taskDescription;
    private String taskStatus;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private List<Assignee> assignees;

    public TaskDTO(String taskTitle, String taskDescription, String taskStatus, LocalDateTime createdOn, LocalDateTime updatedOn, List<Assignee> assignees) {
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.assignees = assignees;
    }

    // Getters and setters for each field

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<Assignee> getAssignees() {
        return assignees;
    }

    public void setAssignees(List<Assignee> assignees) {
        this.assignees = assignees;
    }
}

