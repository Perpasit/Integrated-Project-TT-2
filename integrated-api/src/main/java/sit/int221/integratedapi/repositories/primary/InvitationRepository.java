package sit.int221.integratedapi.repositories.primary;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.int221.integratedapi.entities.Invitation;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Integer> {

    boolean existsByBoardIdAndInviteeEmailAndStatus(String boardId, String inviteeEmail, Invitation.Status status);

    List<Invitation> findByBoardIdAndInviteeEmailAndStatus(String boardId, String inviteeEmail, Invitation.Status status);

    Invitation findByBoardIdAndInviteeNameAndStatus(String boardId, String inviteeName, Invitation.Status status);
}

