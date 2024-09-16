package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Status;

import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByNameIgnoreCase(String name);
    List<Status> findByBoardId(Integer boardId);
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Status s WHERE LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    Optional<Status> findByIdAndBoardId(Integer statusId, Integer boardId);

    Optional<Status> findByNameIgnoreCaseAndBoardId(String name, Integer boardId);
    boolean existsByNameAndBoardId(String name, Integer boardId);

}