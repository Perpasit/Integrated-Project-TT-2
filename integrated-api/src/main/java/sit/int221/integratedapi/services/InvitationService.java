package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.entities.Collaborator;
import sit.int221.integratedapi.entities.Invitation;
import sit.int221.integratedapi.repositories.primary.InvitationRepository;

import java.util.Optional;

@Service
public class InvitationService {
    @Autowired
    private InvitationRepository invitationRepository;


    @Transactional
    public boolean updateAccessRight(String boardId, Long invitationId, String accessRight) {
        Optional<Invitation> invitationOpt = invitationRepository.findByBoardIdAndId(boardId, invitationId);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            if ("READ".equals(accessRight) || "WRITE".equals(accessRight)) {
                invitation.setAccessRight(Collaborator.AccessRight.valueOf(accessRight)); // Ensure AccessRight is an enum
                invitationRepository.save(invitation);
                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean deleteInvitation(String boardId, Long invitationId) {
        Optional<Invitation> invitationOpt = invitationRepository.findByBoardIdAndId(boardId, invitationId);
        if (invitationOpt.isPresent()) {
            invitationRepository.delete(invitationOpt.get());
            return true;
        }
        return false;
    }
}
