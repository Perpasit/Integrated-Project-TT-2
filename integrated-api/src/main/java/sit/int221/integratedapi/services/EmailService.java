package sit.int221.integratedapi.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String collaboratorName, String boardName, String confirmationLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);


            String htmlContent = "<html>" +
                    "<body>" +
                    "<p>Hi <strong>" + collaboratorName + "</strong>,</p>" +
                    "<p>You have been invited to collaborate on the board: <strong>" + boardName + "</strong>.</p>" +
                    "<p>Please click the link below to confirm your invitation:</p>" +
                    "<p><a href='" + confirmationLink + "'>Confirm Invitation</a></p>" +
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
}
