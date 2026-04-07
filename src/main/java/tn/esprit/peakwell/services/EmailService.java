package tn.esprit.peakwell.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import tn.esprit.peakwell.entities.User;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;


@Override
public void sendAccountStatusEmail(String to, String subject, String templateName, Map<String, Object> variables) {

    try {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Inject variables into template
        var context = new org.thymeleaf.context.Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process(templateName, context);

        helper.setFrom("PeakWell <" + fromEmail + ">");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); 

        mailSender.send(message);

    } catch (Exception ex) {
        ex.printStackTrace();
        throw new RuntimeException("Error while sending HTML email");
    }
}

@Override
    public void sendAccountLockedEmail(User user) {

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFirstName());
        vars.put("status", "BLOCKED");
        vars.put("message",
                "Your account has been temporarily locked due to multiple failed login attempts. It will be automatically reactivated after 1 hour.");

        sendAccountStatusEmail(
                user.getEmail(),
                "Your account is temporarily locked 🔒",
                "account-status",
                vars
        );
    }

    //  UNLOCKED EMAIL
    @Override
    public void sendAccountUnlockedEmail(User user) {

        Map<String, Object> vars = new HashMap<>();
        vars.put("name", user.getFirstName());
        vars.put("status", "ACTIVE");
        vars.put("message",
                "Your account has been successfully reactivated. You can now log in again.");

        sendAccountStatusEmail(
                user.getEmail(),
                "Your account has been reactivated",
                "account-status",
                vars
        );
    }



}
