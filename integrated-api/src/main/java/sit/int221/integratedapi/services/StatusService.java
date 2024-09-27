package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.*;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.GlobalSetting;
import sit.int221.integratedapi.entities.Status;
import sit.int221.integratedapi.entities.Task;
import sit.int221.integratedapi.exceptions.*;
import sit.int221.integratedapi.repositories.primary.BoardRepository;
import sit.int221.integratedapi.repositories.primary.StatusRepository;
import sit.int221.integratedapi.repositories.primary.TaskRepository;
import sit.int221.integratedapi.utils.ListMapper;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Qualifier("primaryDataSource")
@Service
public class StatusService {

    private final StatusRepository statusRepository;

    private final TaskRepository taskRepository;

    private final GlobalSettingService globalSettingService;

    private final ListMapper listMapper;

    private final ModelMapper modelMapper;

    private final BoardRepository boardRepository;

    @Autowired
    public StatusService(StatusRepository statusRepository, TaskRepository taskRepository, GlobalSettingService globalSettingService, ListMapper listMapper, ModelMapper modelMapper,BoardRepository boardRepository){
        this.statusRepository = statusRepository;
        this.taskRepository = taskRepository;
        this.globalSettingService = globalSettingService;
        this.listMapper = listMapper;
        this.modelMapper = modelMapper;
        this.boardRepository = boardRepository;
    }

    public List<StatusDTO> getStatusesByBoardId(String boardId) {
        List<Status> statuses = statusRepository.findByBoardId(boardId);
        List<StatusDTO> statusDTOs = new ArrayList<>();
        for (Status status : statuses) {
            StatusDTO statusDTO = modelMapper.map(status, StatusDTO.class);
            statusDTO.setNoOfTasks(getNoOfTasksByStatusId(status.getId())); // Assuming you've changed 'id' to 'statusId'
            statusDTOs.add(statusDTO);
        }
        return statusDTOs;
    }
    public int getNoOfTasksByStatusId(Integer statusId) {
        return taskRepository.countByStatusId(statusId);
    }

//    public List<TaskDTO> getTasksByStatusId(Integer statusId) {
//        Status status = statusRepository.findById(statusId)
//                .orElseThrow(() -> new ItemNotFoundException("Status " + statusId + " does not exist!"));
//
//        List<Task> tasks = taskRepository.findByStatusId(statusId);
//        return listMapper.mapList(tasks, TaskDTO.class);
//    }

    public List<TaskDTO> getTasksByBoardAndStatusId(String boardId, Integer statusId) {
        // ตรวจสอบว่า status นั้นอยู่ในบอร์ดที่กำหนดหรือไม่
        Status status = statusRepository.findByIdAndBoardId(statusId, boardId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + statusId + " does not exist in board " + boardId));

        // ดึงรายการงานที่เกี่ยวข้องกับสถานะนี้
        List<Task> tasks = taskRepository.findByStatusId(statusId);

        // แปลง Task เป็น TaskDTO ก่อนส่งกลับ
        return listMapper.mapList(tasks, TaskDTO.class);
    }


//    public StatusRDTO getStatusById(Integer id) {
//        Status status = statusRepository.findById(id).orElse(null);
//        if (status != null) {
//            StatusRDTO statusRDTO = modelMapper.map(status, StatusRDTO.class);
//            statusRDTO.setGlobalSettingId(status.getGlobalSetting().getId());
//            statusRDTO.setNoOfTasks(getNoOfTasksByStatusId(id));
//            return statusRDTO;
//        }
//        throw new ItemNotFoundException("Status " + id + " does not exist!");
//    }

    public StatusRDTO getStatusByBoardIdAndStatusId(String boardId, Integer statusId) {
        // Find the status by statusId and boardId
        Status status = statusRepository.findByIdAndBoardId(statusId, boardId).orElse(null);
        if (status != null) {
            StatusRDTO statusRDTO = modelMapper.map(status, StatusRDTO.class);
            statusRDTO.setGlobalSettingId(status.getGlobalSetting().getId());
            statusRDTO.setNoOfTasks(getNoOfTasksByStatusId(statusId));
            return statusRDTO;
        }
        throw new ItemNotFoundException("Status with id " + statusId + " for board " + boardId + " does not exist!");
    }

    public void validateBoardOwnership(String boardId, String oidFromToken) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));
        if (board.getVisibility() == Board.Visibility.PUBLIC) {
            return;
        }

        if (oidFromToken == null || !board.getOwnerId().equals(oidFromToken)) {
            throw new ForbiddenException("You do not have permission to access this private board.");
        }
    }
    @Transactional
    public ResponseEntity<Object> createStatus(String boardId, StatusCDTO statusCDTO) {
        // ตรวจสอบค่า boardId ว่ามีอยู่จริงหรือไม่
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ItemNotFoundException("Board with id " + boardId + " does not exist!"));

        ValidationErrorException validationError = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation Error");
        if (statusCDTO.getName() == null || statusCDTO.getName().trim().isEmpty()) {
            validationError.addError("name", "Name must not be null or empty");
        }
        if (!validationError.getErrors().isEmpty()) {
            throw validationError;
        }

        // ตรวจสอบว่ามีสถานะที่มีชื่อเดียวกันในบอร์ดเดียวกันหรือไม่
        if (statusCDTO.getId() == null) {
            if (statusRepository.existsByNameAndBoardId(statusCDTO.getName().trim(), boardId)) {
                throw new ValidationErrorException(HttpStatus.BAD_REQUEST, "Status with name " + statusCDTO.getName() + " already exists in this board!");
            }
        } else {
            // สำหรับการอัปเดตสถานะ ตรวจสอบว่ามีสถานะอื่นที่มีชื่อเดียวกันในบอร์ดเดียวกันหรือไม่
            Status existingStatus = statusRepository.findById(statusCDTO.getId())
                    .orElseThrow(() -> new ItemNotFoundException("Status with id " + statusCDTO.getId() + " does not exist!"));

            if (!existingStatus.getName().equals(statusCDTO.getName().trim()) &&
                    statusRepository.existsByNameAndBoardId(statusCDTO.getName().trim(), boardId)) {
                throw new ValidationErrorException(HttpStatus.BAD_REQUEST, "Status with name " + statusCDTO.getName() + " already exists in this board!");
            }

            existingStatus.setName(statusCDTO.getName().trim());
            existingStatus.setDescription(statusCDTO.getDescription() != null ? statusCDTO.getDescription().trim() : null);
            if (statusCDTO.getColorCode() != null) {
                existingStatus.setColorCode(statusCDTO.getColorCode());
            }
            Status updatedStatus = statusRepository.save(existingStatus);
            StatusCDTO updatedStatusCDTO = modelMapper.map(updatedStatus, StatusCDTO.class);
            return ResponseEntity.ok(updatedStatusCDTO);
        }

        // สำหรับการสร้างสถานะใหม่
        Status status = modelMapper.map(statusCDTO, Status.class);
        status.setName(status.getName().trim());
        if (status.getDescription() != null) {
            status.setDescription(status.getDescription().trim());
        }

        // เชื่อมโยง Status ใหม่กับ Board ที่กำหนด
        status.setBoard(board);
        status.setDescription(status.getDescription() != null && !status.getDescription().trim().isEmpty() ? status.getDescription().trim() : null);
        setDefaultValuesForStatus(status, "General");

        Status createdStatus = statusRepository.save(status);
        StatusCDTO createdStatusCDTO = modelMapper.map(createdStatus, StatusCDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStatusCDTO);
    }



    @Transactional
    public ResponseEntity<StatusDDTO> removeStatus(String boardId, Integer statusId) {
        // ตรวจสอบว่าสถานะนี้อยู่ในบอร์ดที่กำหนดหรือไม่
        Status status = statusRepository.findByIdAndBoardId(statusId, boardId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + statusId + " does not exist in board " + boardId));

        // ตรวจสอบว่าสถานะคือ "No Status" หรือ "Done" หรือไม่
        if ("No Status".equalsIgnoreCase(status.getName()) || "Done".equalsIgnoreCase(status.getName())) {
            throw new NotAllowedOperationException(status.getName() + " cannot be deleted.");
        }

        // ตรวจสอบว่ามีงาน (tasks) ที่เชื่อมโยงกับสถานะนี้หรือไม่
        int taskCount = taskRepository.countByStatusId(statusId);
        if (taskCount > 0) {
            // โยนข้อผิดพลาดเพื่อระบุให้โอนงานไปยังสถานะอื่น
            throw new TransferTasksRequiredException("Destination status for task transfer not specified.");
        }

        // ลบสถานะหากไม่มีงานที่เชื่อมโยง
        statusRepository.delete(status);
        StatusDDTO statusDDTO = listMapper.mapObject(status, StatusDDTO.class);
        return ResponseEntity.ok(statusDDTO);
    }

    @Transactional
    public ResponseEntity<StatusTFDDTO> deleteTransferStatus(String boardId, Integer statusId, Integer newId) {
        // ตรวจสอบว่าสถานะนี้อยู่ในบอร์ดที่กำหนดหรือไม่
        Status statusToDelete = statusRepository.findByIdAndBoardId(statusId, boardId)
                .orElseThrow(() -> new ValidationErrorException(HttpStatus.BAD_REQUEST, "Status " + statusId + " does not exist in board " + boardId));

        // ตรวจสอบว่าสถานะใหม่อยู่ในบอร์ดเดียวกันหรือไม่
        Status newStatus = statusRepository.findByIdAndBoardId(newId, boardId)
                .orElseThrow(() -> new ValidationErrorException(HttpStatus.BAD_REQUEST, "The specified status for task transfer does not exist in board " + boardId));

        // ตรวจสอบว่าสถานะต้นทางและปลายทางต่างกันหรือไม่
        if (statusToDelete.equals(newStatus)) {
            throw new InvalidTransferStatusException("Destination status for task transfer must be different from current status",
                    "The specified status for task transfer does not exist.");
        }

        // ตรวจสอบงานที่เชื่อมโยงกับสถานะต้นทาง
        int deletingStatusTaskCount = taskRepository.countByStatusId(statusId);

        // โอนงานไปยังสถานะใหม่ถ้ามีงานที่เชื่อมโยงกับสถานะนี้
        if (deletingStatusTaskCount > 0) {
            List<Task> tasksToUpdate = taskRepository.findByStatusId(statusId);
            for (Task task : tasksToUpdate) {
                task.setStatus(newStatus);
            }

            // ตรวจสอบว่าสถานะปลายทางเกินขีดจำกัดของงานหรือไม่
            validateStatusNotTransferrable(newStatus, deletingStatusTaskCount + taskRepository.countByStatusId(newId));
        }

        // ลบสถานะต้นทาง
        statusRepository.delete(statusToDelete);

        // ส่งคืน DTO ของสถานะที่ถูกลบ
        StatusTFDDTO statusTFDDTO = modelMapper.map(statusToDelete, StatusTFDDTO.class);
        return ResponseEntity.ok(statusTFDDTO);
    }




    @Transactional
    public ResponseEntity<Object> updateStatus(String boardId, Integer statusId, StatusUDTO statusUDTO) {
        // ตรวจสอบว่าสถานะนี้อยู่ในบอร์ดที่กำหนดหรือไม่
        Status existingStatus = statusRepository.findByIdAndBoardId(statusId, boardId)
                .orElseThrow(() -> new ItemNotFoundException("Status " + statusId + " does not exist in board " + boardId));

        ValidationErrorException validationError = new ValidationErrorException(HttpStatus.BAD_REQUEST, "Validation Error");

        // ตรวจสอบฟิลด์ name ของ statusUDTO
        if (statusUDTO.getName() == null) {
            validationError.addError("name", "must not be null");
            throw validationError;
        } else if (statusUDTO.getName().trim().isEmpty()) {
            validationError.addError("name", "Name must not be empty");
        } else {
            // ตรวจสอบชื่อว่าซ้ำหรือไม่ในบอร์ดนี้
            Status existingStatusWithName = statusRepository.findByNameIgnoreCaseAndBoardId(statusUDTO.getName().trim(), boardId).orElse(null);
            if (existingStatusWithName != null && !existingStatusWithName.getId().equals(statusId)) {
                validationError.addError("name", "must be unique");
                validationError.addError("message", "Status name '" + statusUDTO.getName().trim() + "' already exists in board " + boardId);
            }
        }

        // ตรวจสอบว่าชื่อสถานะเป็น "No Status" หรือ "Done" ซึ่งไม่อนุญาตให้แก้ไข
        if ("No Status".equalsIgnoreCase(existingStatus.getName()) || "Done".equalsIgnoreCase(existingStatus.getName())) {
            throw new NotAllowedOperationException(existingStatus.getName() + " cannot be modified.");
        }

        if (!validationError.getErrors().isEmpty()) {
            throw validationError;
        }

        // อัปเดตฟิลด์ของสถานะ
        existingStatus.setName(statusUDTO.getName().trim());
        existingStatus.setDescription(statusUDTO.getDescription() != null && !statusUDTO.getDescription().trim().isEmpty() ? statusUDTO.getDescription().trim() : null);
        if (statusUDTO.getColorCode() != null) {
            existingStatus.setColorCode(statusUDTO.getColorCode());
        }

        Status savedStatus = statusRepository.save(existingStatus);
        StatusUDTO responseStatusUDTO = modelMapper.map(savedStatus, StatusUDTO.class);
        return ResponseEntity.ok(responseStatusUDTO);
    }



    private void setDefaultValuesForStatus(Status status, String globalSettingName) {
        if (status.getColorCode() == null || status.getColorCode().isEmpty()) {
            status.setColorCode("#000000");
        }
        if (status.getCreatedOn() == null) {
            status.setCreatedOn(ZonedDateTime.now());
        }
        if (status.getUpdatedOn() == null) {
            status.setUpdatedOn(ZonedDateTime.now());
        }
        if (status.getDescription() == null || status.getDescription().isEmpty()) {
            status.setDescription(null);
        }

        GlobalSetting globalSetting = globalSettingService.getGlobalSettingByName(globalSettingName);
        status.setGlobalSetting(globalSetting);
    }

//    private void validateStatusName(String statusName, Integer id) {
//        if (statusName == null || statusName.trim().isEmpty()) {
//            throw new IllegalArgumentException("Status name cannot be null or empty");
//        }
//
//
//        Status existingStatus = statusRepository.findByNameIgnoreCase(statusName.trim()).orElse(null);
//        if (existingStatus != null && !existingStatus.getId().equals(id)) {
//            throw new DuplicateStatusException("Status name '" + statusName.trim() + "' already exists");
//        }
//    }
//
//    private void validateStatusDescription(String description, int maxLength) {
//        if (description != null && description.length() > maxLength) {
//            throw new IllegalArgumentException("Status description must be between 0 and " + maxLength + " characters");
//        }
//    }
//
//    private void validateStatusNotDeletable(Status status) {
//        if (status.getName().equalsIgnoreCase("No Status") || status.getName().equalsIgnoreCase("Done")) {
//            throw new IllegalArgumentException("Status with name '" + status.getName() + "' cannot be deleted or transferred");
//        }
//    }

    private void validateStatusNotTransferrable(Status status, int taskCount) {
        if (status != null && status.getGlobalSetting().isLimitMaximumTask() && taskCount > status.getGlobalSetting().getMaximumTaskLimit()) {
            throw new TransferTasksRequiredException("The destination status cannot be over the limit after transfer");
        }
    }

}