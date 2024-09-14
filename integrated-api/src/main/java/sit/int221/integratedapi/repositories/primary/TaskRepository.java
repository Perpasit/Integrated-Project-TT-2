package sit.int221.integratedapi.repositories.primary;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Task;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByStatusId(Integer statusId);
    List<Task> findByStatusNameIn(List<String> statusNames, Sort sort);

    List<Task> findByStatusIdIn(List<Integer> statusIds, Sort sort);

    List<Task> findByStatusIdInOrStatusNameIn(List<Integer> statusIds, List<String> statusNames, Sort sort);

    int countByStatusId(Integer id);

    List<Task> findByBoardId(Integer boardId);


    public List<Task> findByBoardIdAndStatusNameIn(Integer boardId, List<String> statusNames, Sort sort);



}