package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.TaskUser;
import sit.int221.integratedapi.entities.Board;

import java.util.List;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Integer> {
    List<Collaborator> findByBoard(Board board);

    Collaborator findByUserAndBoard(TaskUser user, Board board);

    boolean existsByUserAndBoard(TaskUser user, Board board);
}
