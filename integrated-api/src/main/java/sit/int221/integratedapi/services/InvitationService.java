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

    @Autowired
    private EmailService emailService;


    @Transactional
    public boolean updateAccessRight(String boardId, Long invitationId, String accessRight) {
        Optional<Invitation> invitationOpt = invitationRepository.findByBoardIdAndId(boardId, invitationId);

        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            if ("READ".equals(accessRight) || "WRITE".equals(accessRight)) {
                invitation.setAccessRight(Collaborator.AccessRight.valueOf(accessRight));
                invitationRepository.save(invitation);

                emailService.sendAccessRightChangeEmail(
                        invitation.getInviteeEmail(),
                        invitation.getBoard(),
                        invitation.getId(),
                        invitation.getInviteeName(),
                        accessRight
                );

                return true;
            }
        }
        return false;
    }

    @Transactional
    public boolean deleteInvitation(String boardId, Long invitationId) {
        Optional<Invitation> invitationOpt = invitationRepository.findByBoardIdAndId(boardId, invitationId);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();
            String collaboratorEmail = invitation.getInviteeEmail();
            String collaboratorName = invitation.getInviteeName();
            String boardName = invitation.getBoard().getName();

            invitationRepository.delete(invitation);
            emailService.sendCancelInvitationsEmail(collaboratorEmail, collaboratorName, boardName);
            return true;
        }
        return false;
    }

}
