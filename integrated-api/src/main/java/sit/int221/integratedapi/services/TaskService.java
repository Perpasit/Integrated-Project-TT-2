package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.TaskCDTO;
import sit.int221.integratedapi.dtos.TaskDDTO;
import sit.int221.integratedapi.dtos.TaskRDTO;
import sit.int221.integratedapi.dtos.TaskUDTO;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.StatusRepository;
import sit.int221.integratedapi.repositories.primary.TaskRepository;
import sit.int221.integratedapi.repositories.primary.TaskUserRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
@Qualifier("primaryDataSource")
@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private StatusRepository statusRepository;
    @Autowired
    private ListMapper listMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private StatusService statusService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    TaskUserRepository taskUserRepository;

    public Task getTaskById(Integer id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException("Task " + id + " does not exist !!!!"));
    }

    public TaskRDTO getTaskDetailsById(String boardId, Integer taskId, String oid) {
        Task task = getTaskById(taskId);
        Board board = task.getBoard();

        if (!Objects.equals(boardId, task.getBoard().getId())) {
            throw new ItemNotFoundException("Task " + taskId + " does not belong to board " + boardId);
        }

        if (!board.getOwnerId().equals(oid)) {
            throw new BoardOwnershipException("User does not own this board.");
        }

        return modelMapper.map(task, TaskRDTO.class);
    }

    public void validateBoardOwnership(String boardId, String oidFromToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // ตรวจสอบว่า board เป็นของเจ้าของที่มี oid ตรงกับ token หรือไม่
        if (!board.getOwnerId().equals(oidFromToken)) {
            throw new ItemNotFoundException("Board with id " + boardId + " does not belong to the user with oid " + oidFromToken);
        }
    }
    public ResponseEntity<Object> getAllTasks(String boardId, String sortBy, List<String> filterStatuses, List<String> allParams) {
        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        Sort sort;
        if (sortBy != null && sortBy.equalsIgnoreCase("asc")) {
            sort = Sort.by(Sort.Direction.ASC, "status.name");
        } else if (sortBy != null && sortBy.equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, "status.name");
        } else {
            sort = Sort.by("createdOn").ascending();
        }

        List<Task> tasks;
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            List<String> statusNames = extractStatusNames(filterStatuses);
            tasks = taskRepository.findByStatusNameInAndBoardId(statusNames, boardId, sort);
        } else {
            tasks = taskRepository.findByBoardId(boardId, sort);
        }
        return ResponseEntity.ok(tasks);
    }

    private List<String> extractStatusNames(List<String> filterStatuses) {
        List<String> statusNames = new ArrayList<>();
        for (String status : filterStatuses) {
            if (!status.matches("\\d+")) {
                statusNames.add(status);
            }
        }
        return statusNames;
    }



    @Transactional
    public ResponseEntity<Object> createNewTask(TaskCDTO taskCDTO, String boardId) {
        // สร้าง ValidationErrorException สำหรับตรวจสอบ validation
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        // ตรวจสอบ title ว่ามีค่าหรือไม่
        if (taskCDTO.getTitle() == null || taskCDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null or empty");
        }

        // ตรวจสอบ boardId ว่ามีค่าหรือไม่
        if (boardId == null) {
            validationErrors.addError("boardId", "must not be null");
        }

        // หากมี validation errors ให้โยน exception ออกไป
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        // ค้นหา Board ที่ต้องการ
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        // ค้นหา Default Status ด้วยชื่อ "no status" และ boardId
        Status status = getDefaultStatus(boardId);

        // ตรวจสอบและตั้งค่า status หากได้รับ status จาก DTO
        if (taskCDTO.getStatus() != null && !taskCDTO.getStatus().trim().isEmpty()) {
            try {
                // ค้นหาสถานะด้วยชื่อแทน ID
                String statusName = taskCDTO.getStatus().trim();
                status = statusRepository.findByNameIgnoreCaseAndBoardId(statusName, boardId)
                        .orElseThrow(() -> new ItemNotFoundException("Status with name '" + statusName + "' does not exist for board id " + boardId));
            } catch (Exception e) {
                validationErrors.addError("status", "invalid status format or name");
                throw validationErrors;
            }
        }

        // ตรวจสอบว่า status มีการจำกัดจำนวน task หรือไม่
        if (status.getGlobalSetting().isLimitMaximumTask() &&
                taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + status.getName() + " has reached its maximum task limit.");
        }

        // สร้าง Task ใหม่
        Task task = new Task();
        task.setTitle(taskCDTO.getTitle().trim());
        task.setStatus(status);  // ตั้งค่า status
        task.setDescription(taskCDTO.getDescription() != null && !taskCDTO.getDescription().trim().isEmpty() ? taskCDTO.getDescription().trim() : null);
        task.setAssignees(taskCDTO.getAssignees() != null && !taskCDTO.getAssignees().trim().isEmpty() ? taskCDTO.getAssignees().trim() : null);
        task.setBoard(board);  // ตั้งค่า board
        setDefaultValuesForTask(task);  // ตั้งค่า default values

        // บันทึก task ลงฐานข้อมูล
        Task createdTask = taskRepository.save(task);

        // แปลง Task เป็น TaskCDTO สำหรับการตอบกลับ
        TaskCDTO createdTaskCDTO = modelMapper.map(createdTask, TaskCDTO.class);

        // ตอบกลับ HTTP Status 201 พร้อมข้อมูล task ที่สร้างใหม่
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskCDTO);
    }







    @Transactional
    public ResponseEntity<TaskDDTO> removeTask(String boardId, Integer taskId) {
        // Fetch the task from the repository
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist!"));

        // Verify that the task belongs to the specified board
        if (task.getBoard() == null || !Objects.equals(task.getBoard().getId(), boardId)) {
            throw new ItemNotFoundException("Task with id " + taskId + " does not belong to board with id " + boardId + " or does not exist.");
        }

        // Delete the task
        taskRepository.delete(task);

        // Map the deleted task to DTO
        TaskDDTO taskDDTO = modelMapper.map(task, TaskDDTO.class);

        // Return a response with the details of the deleted task
        return ResponseEntity.ok(taskDDTO);
    }



    @Transactional
    public ResponseEntity<Object> updateTask(String boardId, Integer taskId, TaskUDTO updatedTaskDTO) {
        // ตรวจสอบว่ามี Task ที่ต้องการหรือไม่
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new ItemNotFoundException("Task with id " + taskId + " does not exist!"));

        // ตรวจสอบว่า Task ที่จะอัปเดตเป็นของ Board ที่ถูกต้อง
        if (existingTask.getBoard() == null || !Objects.equals(existingTask.getBoard().getId(), boardId)) {
            throw new ItemNotFoundException("Task does not belong to Board with id " + boardId + "!");
        }

        // สร้าง ValidationErrorException สำหรับตรวจสอบ validation
        ValidationErrorException validationErrors = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation failed");

        // ตรวจสอบ title ว่ามีค่าหรือไม่
        if (updatedTaskDTO.getTitle() == null || updatedTaskDTO.getTitle().trim().isEmpty()) {
            validationErrors.addError("title", "must not be null or empty");
        }

        // หากมี validation errors ให้โยน exception ออกไป
        if (!validationErrors.getErrors().isEmpty()) {
            throw validationErrors;
        }

        // ตรวจสอบ Status และจัดการข้อผิดพลาด
        Status status = getDefaultStatus(boardId); // ค้นหา Status "no status" ที่ตรงกันสำหรับ boardId

        if (updatedTaskDTO.getStatus() != null && !updatedTaskDTO.getStatus().trim().isEmpty()) {
            try {
                // ค้นหาสถานะด้วยชื่อแทนการใช้ ID
                String statusName = updatedTaskDTO.getStatus().trim();
                status = statusRepository.findByNameIgnoreCaseAndBoardId(statusName, boardId)
                        .orElseThrow(() -> new ItemNotFoundException("Status with name '" + statusName + "' does not exist for board id " + boardId));
            } catch (Exception e) {
                validationErrors.addError("status", "invalid status format or name");
                throw validationErrors;
            }
        }

        // ตรวจสอบข้อจำกัดของจำนวน tasks ใน status
        if (status.getGlobalSetting().isLimitMaximumTask() &&
                taskRepository.countByStatusId(status.getId()) >= status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new StatusLimitReachedException("The status " + status.getName() + " has reached its maximum task limit.");
        }

        // อัปเดต Task ด้วยข้อมูลใหม่
        existingTask.setTitle(updatedTaskDTO.getTitle().trim());
        existingTask.setDescription(updatedTaskDTO.getDescription() != null && !updatedTaskDTO.getDescription().trim().isEmpty() ? updatedTaskDTO.getDescription().trim() : null);
        existingTask.setAssignees(updatedTaskDTO.getAssignees() != null && !updatedTaskDTO.getAssignees().trim().isEmpty() ? updatedTaskDTO.getAssignees().trim() : null);
        existingTask.setStatus(status);  // อัปเดต status
        existingTask.setUpdatedOn(ZonedDateTime.now());

        // บันทึก Task ที่อัปเดตแล้ว
        Task updatedTask = taskRepository.save(existingTask);

        // แปลง Task ที่อัปเดตเป็น DTO
        TaskUDTO updatedTaskCDTO = modelMapper.map(updatedTask, TaskUDTO.class);

        // ส่งผลลัพธ์กลับ
        return ResponseEntity.ok(updatedTaskCDTO);
    }




    private Status getDefaultStatus(String boardId) {
        return statusRepository.findByNameIgnoreCaseAndBoardId("no status", boardId)
                .orElseThrow(() -> new IllegalStateException("Default 'no status' for board " + boardId + " not found in the database"));
    }


    private void setDefaultValuesForTask(Task task) {
        task.setTitle(task.getTitle().trim());
        task.setDescription(task.getDescription() != null && !task.getDescription().trim().isEmpty() ? task.getDescription().trim() : null);
        task.setAssignees(task.getAssignees() != null && !task.getAssignees().trim().isEmpty() ? task.getAssignees().trim() : null);
        if (task.getCreatedOn() == null) {
            task.setCreatedOn(ZonedDateTime.now());
        }
        task.setUpdatedOn(ZonedDateTime.now());
    }

    public ResponseEntity<Object> getTasksByBoardId(String boardId, String sortBy, List<String> filterStatuses) {
        // Return an empty list if any status contains '#'
        if (filterStatuses != null && filterStatuses.stream().anyMatch(status -> status.contains("#"))) {
            return ResponseEntity.ok(new ArrayList<>());
        }

        // Determine sorting direction based on the sortBy parameter
        Sort sort;
        if (sortBy != null && sortBy.equalsIgnoreCase("asc")) {
            sort = Sort.by(Sort.Direction.ASC, "status.name");
        } else if (sortBy != null && sortBy.equalsIgnoreCase("desc")) {
            sort = Sort.by(Sort.Direction.DESC, "status.name");
        } else {
            sort = Sort.by("createdOn").ascending();  // Default sorting by creation date
        }

        // Retrieve tasks by boardId with optional status filtering
        List<Task> tasks;

        // Extract status names if provided, or fetch all tasks if filterStatuses is null or empty
        if (filterStatuses != null && !filterStatuses.isEmpty()) {
            tasks = taskRepository.findByBoardIdAndStatusNameIn(boardId, filterStatuses, sort);
        } else {
            tasks = taskRepository.findByBoardId(boardId, sort);
        }

        // Return the result
        return ResponseEntity.ok(tasks);
    }



    public boolean isBoardOwnedByOid(String boardId, String oidFromToken) {
        // Fetch the board by ID
        Optional<Board> boardOptional = boardRepository.findById(boardId);

        // If board is not found, return false
        if (!boardOptional.isPresent()) {
            return false;
        }

        Board board = boardOptional.get();

        // Fetch the TaskUser associated with the board's taskusersOid
        Optional<TaskUser> taskUserOptional = Optional.ofNullable(taskUserRepository.findByOid(board.getOwnerId()));

        // If the taskUser is found, check if it matches the OID
        return taskUserOptional.isPresent() && taskUserOptional.get().getOid().equals(oidFromToken);
    }


    // Method to fetch tasks by boardId
    public List<TaskRDTO> getTasksByBoardId(String boardId) {
        // Fetch tasks by boardId (directly, without Optional)
        List<Task> tasks = taskRepository.findByBoardId(boardId);

        // Convert to TaskRDTOs
        List<TaskRDTO> taskRDTOs = new ArrayList<>();
        for (Task task : tasks) {
            TaskRDTO taskRDTO = new TaskRDTO();
            taskRDTO.setTitle(task.getTitle());
            taskRDTO.setDescription(task.getDescription());
            taskRDTO.setStatus(String.valueOf(task.getStatus()));
            // Populate other TaskRDTO fields as necessary
            taskRDTOs.add(taskRDTO);
        }

        return taskRDTOs;
    }



}