package sit.int221.intregratedapi.dtos;

import lombok.Data;



@Data
public class TaskCUDDTO {
    private Integer id;
    private String title;
    private String description;
    private String assignees;
    private StatusDTO status; // Using StatusDTO instead of TaskStatus enum

    public void setDescription(String description) {
        this.description = description.trim();
    }
}