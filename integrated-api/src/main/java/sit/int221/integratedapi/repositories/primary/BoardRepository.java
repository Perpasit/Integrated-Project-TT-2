package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Integer> {
}
