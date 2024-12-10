package sit.int221.integratedapi.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.dtos.InvitationDDTO;
import sit.int221.integratedapi.dtos.InvitationUDTO;
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
    public InvitationUDTO updateAccessRight(String boardId, Long invitationId, String accessRight) {
        Optional<Invitation> invitationOpt = invitationRepository.findByBoardIdAndId(boardId, invitationId);

        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();

            // Check if the new accessRight is the same as the current one
            if (invitation.getAccessRight().name().equals(accessRight)) {
                InvitationUDTO invitationUDTO = new InvitationUDTO();
                invitationUDTO.setInviterName(invitation.getInviterName());
                invitationUDTO.setInviteeName(invitation.getInviteeName());
                invitationUDTO.setInviteeEmail(invitation.getInviteeEmail());
                invitationUDTO.setBoardName(invitation.getBoard().getName());
                invitationUDTO.setAccessRight(accessRight);
                invitationUDTO.setStatus(invitation.getStatus().name());

                return invitationUDTO;
            }

            // Update access right if it differs from the current one
            if ("READ".equals(accessRight) || "WRITE".equals(accessRight)) {
                invitation.setAccessRight(Collaborator.AccessRight.valueOf(accessRight));
                invitationRepository.save(invitation);

                // Send notification email about the access right change
                emailService.sendAccessRightChangeEmail(
                        invitation.getInviteeEmail(),
                        invitation.getBoard(),
                        invitation.getId(),
                        invitation.getInviteeName(),
                        accessRight
                );

                // Populate InvitationUDTO with updated information
                InvitationUDTO invitationUDTO = new InvitationUDTO();
                invitationUDTO.setInviterName(invitation.getInviterName());
                invitationUDTO.setInviteeName(invitation.getInviteeName());
                invitationUDTO.setInviteeEmail(invitation.getInviteeEmail());
                invitationUDTO.setBoardName(invitation.getBoard().getName());
                invitationUDTO.setAccessRight(accessRight);
                invitationUDTO.setStatus(invitation.getStatus().name());

                return invitationUDTO;
            }
        }
        return null; // Return null if the invitation was not found or accessRight was invalid
    }



    @Transactional
    public InvitationDDTO deleteInvitation(String boardId, Long invitationId) {
        Optional<Invitation> invitationOpt = invitationRepository.findByBoardIdAndId(boardId, invitationId);
        if (invitationOpt.isPresent()) {
            Invitation invitation = invitationOpt.get();

            String collaboratorEmail = invitation.getInviteeEmail();
            String collaboratorName = invitation.getInviteeName();
            String boardName = invitation.getBoard().getName();

            InvitationDDTO invitationDDTO = new InvitationDDTO();
            invitationDDTO.setInviterName(invitation.getInviterName());
            invitationDDTO.setInviteeName(collaboratorName);
            invitationDDTO.setBoardName(boardName);
            invitationDDTO.setAccessRight(invitation.getAccessRight().name());
            invitationDDTO.setStatus(invitation.getStatus().name());

            invitationRepository.delete(invitation);
            emailService.sendCancelInvitationsEmail(collaboratorEmail, collaboratorName, boardName);

            return invitationDDTO;
        }
        return null;
    }

}
