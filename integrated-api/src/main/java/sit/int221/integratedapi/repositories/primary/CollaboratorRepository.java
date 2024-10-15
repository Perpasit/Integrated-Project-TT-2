package sit.int221.integratedapi.repositories.primary;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.Board;
import sit.int221.integratedapi.entities.User;

import java.util.List;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Integer> {
    List<Collaborator> findByBoard(Board board);

    // แก้ไขเมธอดให้ใช้ userOid แทน User entity
    Collaborator findByUserOidAndBoard(String userOid, Board board);

    boolean existsByBoardIdAndEmail(String boardId, String email);

    List<Collaborator> findByUserOid(String userOid);

    @Query("SELECT c FROM Collaborator c WHERE c.userOid = :userOid AND c.board.id = :boardId")
    Collaborator findByUserOidAndBoardId(@Param("userOid") String userOid, @Param("boardId") String boardId);



}
