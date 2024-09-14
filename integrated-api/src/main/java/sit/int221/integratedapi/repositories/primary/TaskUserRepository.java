package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.TaskUser;

@Repository
public interface TaskUserRepository extends JpaRepository<TaskUser, String> {
    TaskUser findByOid(String oid);
}
