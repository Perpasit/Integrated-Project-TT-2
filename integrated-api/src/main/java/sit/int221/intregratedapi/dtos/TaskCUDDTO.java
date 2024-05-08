//package sit.int221.intregratedapi.dtos;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import sit.int221.intregratedapi.entities.TaskStatus;
//
//
//@NoArgsConstructor
//@AllArgsConstructor
//@Data
//@Builder
//public class TaskCUDDTO {
//    private Integer id;
//    private String title;
//    private String description;
//    private String assignees;
//    private TaskStatus status;
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//
//    public void setStatus(String statusStr) {
//        if (statusStr == null || statusStr.isEmpty()) {
//            this.status = TaskStatus.NO_STATUS;
//        } else {
//            try {
//                this.status = TaskStatus.valueOf(statusStr.toUpperCase());
//            } catch (IllegalArgumentException e) {
//                // Handle invalid status value
//                this.status = TaskStatus.NO_STATUS;
//            }
//        }
//    }
//}
