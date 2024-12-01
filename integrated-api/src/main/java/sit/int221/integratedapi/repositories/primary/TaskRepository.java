package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByStatusId(Integer statusId);

    List<Task> findByStatusIdIn(List<Integer> statusIds, Sort sort);

    List<Task> findByStatusIdInOrStatusNameIn(List<Integer> statusIds, List<String> statusNames, Sort sort);

    int countByStatusId(Integer id);

    List<Task> findByBoardId(String boardId);

    List<Task> findByBoardIdAndStatusNameIn(String boardId, List<String> statusNames, Sort sort);

    List<Task> findByStatusNameInAndBoardId(List<String> statusNames, String boardId, Sort sort);

    List<Task> findByBoardId(String boardId, Sort sort); // เปลี่ยนจาก Integer เป็น String

    List<Task> findFirstByBoardIdAndStatusNameIn(String boardId, List<String> statusNames, Sort sort);

    @Query("SELECT COUNT(a) FROM TaskAttachment a WHERE a.task.id = :taskId")
    int countAttachmentsByTaskId(@Param("taskId") Integer taskId);
}
