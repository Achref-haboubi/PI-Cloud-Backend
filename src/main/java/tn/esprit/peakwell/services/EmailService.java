package tn.esprit.peakwell.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${app.name:PeakWell Forum}")
    private String appName;

    @Async
    public void send(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) {
            log.warn("Email skipped — no recipient address");
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(msg);
            log.info("Email sent to {} — {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    // ── Moderation Alert ──────────────────────────────────────────────────────

    public void sendInappropriateContentAlert(
            String commentContent,
            String commentAuthor,
            String articleId,
            String category,
            List<String> detectedWords) {

        String subject = "🚨 Inappropriate Comment Detected — " + appName;

        String detectedWordsHtml = detectedWords.stream()
            .map(word -> "<span style='background:#c96a3f;color:white;padding:3px 10px;" +
                         "border-radius:12px;font-size:12px;margin:2px;display:inline-block;font-weight:600;'>" +
                         word + "</span>")
            .collect(Collectors.joining(" "));

        String categoryEmoji = switch (category) {
            case "HATE_SPEECH" -> "🚫";
            case "VIOLENCE" -> "⚔️";
            case "SEXUAL" -> "🔞";
            case "PROFANITY" -> "🤬";
            case "SPAM" -> "📧";
            default -> "⚠️";
        };

        String heading = "Inappropriate content has been blocked";
        String bodyContent = "A comment containing <strong>" + category.replace("_", " ").toLowerCase() +
                           "</strong> has been automatically detected and blocked on <strong>" + appName + "</strong>.<br><br>" +
                           "<strong>Comment:</strong> \"" + (commentContent != null ? commentContent : "") + "\"<br><br>" +
                           "<strong>Author:</strong> " + (commentAuthor != null ? commentAuthor : "Anonymous") + "<br>" +
                           "<strong>Article ID:</strong> #" + (articleId != null ? articleId : "Unknown") + "<br>" +
                           "<strong>Detected At:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")) +
                           "<br><br><strong>Flagged Words:</strong><br>" + detectedWordsHtml +
                           "<br><br><span style='color:#8a7e78;font-size:12px;'>The user has been notified. No further action required.</span>";

        String body = template(
            "Admin",
            heading,
            bodyContent,
            "#c96a3f",
            categoryEmoji + " " + category.replace("_", " ")
        );

        send(adminEmail, subject, body);
    }

    // ── HTML Template ────────────────────────────────────────────────────────

    private String template(String name, String heading, String body, String accentColor, String badge) {
        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f5f1ed;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f5f1ed;padding:32px 0;">
                <tr><td align="center">
                  <table width="560" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                    <!-- Header -->
                    <tr>
                      <td style="background:linear-gradient(135deg,#c96a3f,#e88f68);padding:32px 40px;text-align:center;">
                        <h1 style="margin:0;color:#ffffff;font-size:26px;font-weight:700;letter-spacing:-0.5px;">🌿 PeakWell</h1>
                        <p style="margin:6px 0 0;color:rgba(255,255,255,0.85);font-size:13px;">Health &amp; Wellness Platform</p>
                      </td>
                    </tr>
                    <!-- Badge -->
                    <tr>
                      <td style="text-align:center;padding:24px 40px 0;">
                        <span style="display:inline-block;background:%s22;color:%s;border:1.5px solid %s44;border-radius:100px;padding:6px 20px;font-size:13px;font-weight:600;">%s</span>
                      </td>
                    </tr>
                    <!-- Body -->
                    <tr>
                      <td style="padding:24px 40px 32px;">
                        <p style="margin:0 0 8px;color:#8a7e78;font-size:13px;">Hello, <strong>%s</strong></p>
                        <h2 style="margin:0 0 16px;color:#1e1a16;font-size:20px;font-weight:700;">%s</h2>
                        <p style="margin:0;color:#5a5450;font-size:14px;line-height:1.7;">%s</p>
                      </td>
                    </tr>
                    <!-- Footer -->
                    <tr>
                      <td style="background:#f9f6f3;padding:16px 40px;border-top:1px solid #ede8e3;text-align:center;">
                        <p style="margin:0;color:#b5aaa5;font-size:11px;">This is an automated message from PeakWell. Please do not reply to this email.</p>
                      </td>
                    </tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(accentColor, accentColor, accentColor, badge, name, heading, body);
    }
}
