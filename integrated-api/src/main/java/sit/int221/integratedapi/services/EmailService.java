package sit.int221.integratedapi.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sit.int221.integratedapi.entities.Board;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String collaboratorName, String boardName, String confirmationLink, String accessRight) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = "<html>" +
                    "<body>" +
                    "<p>Hi <strong>" + collaboratorName + "</strong>,</p>" +
                    "<p>You have been invited to collaborate on the board: <strong>" + boardName + "</strong>.</p>" +
                    "<p>Your access rights for this board are: <strong>" + accessRight + "</strong>.</p>" + // Include access rights
                    "<p>Please click the link below to confirm your invitation:</p>" +
                    "<p><a href='" + confirmationLink + "'>Click here to confirm</a></p>" +
                    "<p>If you did not expect this invitation, please ignore this email.</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("noreply@intproj23.sit.kmutt.ac.th", "ITBKK-TT2", "UTF-8"));
            helper.setReplyTo("noreply@intproj23.sit.kmutt.ac.th");

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }


    public void sendChangeEmail(String to, String subject, String collaboratorName, String boardName, String confirmationLink, String newAccessRight) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = "<html>" +
                    "<body>" +
                    "<p>Hi <strong>" + collaboratorName + "</strong>,</p>" +
                    "<p>You have been invited to collaborate on the board: <strong>" + boardName + "</strong>.</p>" +
                    "<p>Your access rights for this board are currently set to: <strong>" + newAccessRight + "</strong>.</p>" +
                    "<p>To confirm your participation, please click the link below to accept or decline this invitation:</p>" +
                    "<p><a href='" + confirmationLink + "'>Respond to Invitation</a></p>" +
                    "<p>If you have any questions about this invitation, please contact the board owner.</p>" +
                    "<p>Thank you for considering this collaboration!</p>" +
                    "</body>" +
                    "</html>";


            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("noreply@intproj23.sit.kmutt.ac.th", "ITBKK-TT2", "UTF-8"));
            helper.setReplyTo("noreply@intproj23.sit.kmutt.ac.th");

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send access rights change email", e);
        }
    }

    public void sendCancelEmail(String to, String collaboratorName, String boardName, String subject) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = "<html>" +
                    "<body>" +
                    "<p>Hi <strong>" + collaboratorName + "</strong>,</p>" +
                    "<p>We wanted to let you know that your invitation to collaborate on the board: <strong>" + boardName + "</strong> has been canceled by the board owner.</p>" +
                    "<p>If you have any questions regarding this change, please contact the board owner directly.</p>" +
                    "<p>Thank you for your understanding.</p>" +
                    "</body>" +
                    "</html>";

            helper.setText(htmlContent, true);
            helper.setFrom(new InternetAddress("noreply@intproj23.sit.kmutt.ac.th", "ITBKK-TT2", "UTF-8"));
            helper.setReplyTo("noreply@intproj23.sit.kmutt.ac.th");

            mailSender.send(message);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to send cancellation email", e);
        }
    }



    public void sendConfirmationEmail(String email, Board board, String inviteeName, String accessRight) {
        String confirmationLink = baseUrl + "/board/" + board.getId() + "/collab/invitations";
        String subject = "Confirm Your Invitation to Collaborate";

        sendEmail(email, subject, inviteeName, board.getName(), confirmationLink, accessRight);
    }

    public void sendAccessRightChangeEmail(String email, Board board, Integer invitationId, String inviteeName, String newAccessRight) {
        String confirmationLink = baseUrl + "/board/" + board.getId() + "/collab/invitations?invitationId=" + invitationId;
        String subject = "Your Access Rights Have Been Updated";

        sendChangeEmail(email, subject, inviteeName, board.getName(), confirmationLink, newAccessRight);
    }

    public void sendCancelInvitationsEmail(String to, String collaboratorName, String boardName) {
        String subject = "Your Invitation to Collaborate on " + boardName + " Has Been Canceled";

        sendCancelEmail(to, collaboratorName, boardName, subject);
    }





}
