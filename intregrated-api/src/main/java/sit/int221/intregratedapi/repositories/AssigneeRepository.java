package sit.int221.intregratedapi.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.intregratedapi.entities.Assignee;


@Repository
public interface AssigneeRepository extends JpaRepository<Assignee, Long> {
}
