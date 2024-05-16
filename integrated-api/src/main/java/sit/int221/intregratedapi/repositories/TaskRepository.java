package sit.int221.intregratedapi.repositories;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.intregratedapi.entities.Task;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByStatusId(Integer statusId);
    List<Task> findByStatusNameIn(List<String> statusNames, Sort sort);

    int countByStatusId(Integer id);
}