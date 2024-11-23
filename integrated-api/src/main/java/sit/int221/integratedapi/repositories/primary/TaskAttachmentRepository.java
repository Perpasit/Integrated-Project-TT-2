package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.TaskAttachment;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Integer> {
    List<TaskAttachment> findByTaskId(Integer taskId);
    Optional<TaskAttachment> findById(Integer id);
}
