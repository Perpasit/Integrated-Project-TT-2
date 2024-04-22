package sit.int221.intregratedapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.intregratedapi.entities.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
}
