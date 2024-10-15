package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Board;

import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<Board, String> {
    List<Board> findByOwnerId(String taskusersOid);
    boolean existsByOwnerId(String taskusersOid);

}
