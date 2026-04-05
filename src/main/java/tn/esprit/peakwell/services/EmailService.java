package tn.esprit.peakwell.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

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
}
