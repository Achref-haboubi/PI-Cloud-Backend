package tn.esprit.peakwell.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${app.name:PeakWell Forum}")
    private String appName;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendInappropriateContentAlert(
            String commentContent,
            String commentAuthor,
            String articleId,
            String category,
            List<String> detectedWords) {

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("🚨 [" + appName + "] Inappropriate Comment Detected - Action Required");

            String htmlContent = buildEmailTemplate(
                commentContent, commentAuthor, articleId, category, detectedWords
            );

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ Admin alert email sent successfully to: " + adminEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send admin alert email: " + e.getMessage());
            e.printStackTrace();
            // Don't throw exception - email failure should not affect comment blocking
        }
    }

    private String buildEmailTemplate(
            String commentContent,
            String commentAuthor,
            String articleId,
            String category,
            List<String> detectedWords) {

        String detectedWordsHtml = detectedWords.stream()
            .map(word -> "<span style='background:#f44336;color:white;padding:2px 8px;" +
                         "border-radius:12px;font-size:12px;margin:2px;display:inline-block'>" +
                         word + "</span>")
            .collect(Collectors.joining(" "));

        String categoryColor = switch (category) {
            case "HATE_SPEECH" -> "#9c27b0";
            case "VIOLENCE" -> "#f44336";
            case "SEXUAL" -> "#ff5722";
            case "PROFANITY" -> "#ff9800";
            case "SPAM" -> "#607d8b";
            default -> "#795548";
        };

        String categoryEmoji = switch (category) {
            case "HATE_SPEECH" -> "🚫";
            case "VIOLENCE" -> "⚔️";
            case "SEXUAL" -> "🔞";
            case "PROFANITY" -> "🤬";
            case "SPAM" -> "📧";
            default -> "⚠️";
        };

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;background:#f5f5f5;font-family:Arial,sans-serif;">
                
                <div style="max-width:600px;margin:20px auto;background:white;
                            border-radius:12px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.1);">
                    
                    <!-- Header -->
                    <div style="background:#C75B30;padding:24px;text-align:center;">
                        <h1 style="color:white;margin:0;font-size:22px;">
                            🚨 Inappropriate Content Alert
                        </h1>
                        <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:14px;">
                            A comment has been automatically blocked on %s
                        </p>
                    </div>
                    
                    <!-- Content -->
                    <div style="padding:28px;">
                        
                        <!-- Category Badge -->
                        <div style="margin-bottom:20px;text-align:center;">
                            <span style="background:%s;color:white;padding:8px 20px;
                                         border-radius:20px;font-size:14px;font-weight:bold;">
                                %s %s
                            </span>
                        </div>

                        <!-- Comment Details -->
                        <table style="width:100%%;border-collapse:collapse;margin-bottom:20px;">
                            <tr>
                                <td style="padding:10px;background:#f9f9f9;border-radius:6px 0 0 0;
                                            font-weight:bold;color:#555;width:35%%;font-size:13px;">
                                    👤 Author
                                </td>
                                <td style="padding:10px;background:#f9f9f9;border-radius:0 6px 0 0;
                                            color:#333;font-size:13px;">
                                    %s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:10px;font-weight:bold;color:#555;font-size:13px;">
                                    📄 Article ID
                                </td>
                                <td style="padding:10px;color:#333;font-size:13px;">
                                    #%s
                                </td>
                            </tr>
                            <tr>
                                <td style="padding:10px;background:#f9f9f9;font-weight:bold;
                                            color:#555;font-size:13px;">
                                    🕐 Detected At
                                </td>
                                <td style="padding:10px;background:#f9f9f9;color:#333;font-size:13px;">
                                    %s
                                </td>
                            </tr>
                        </table>

                        <!-- Comment Content -->
                        <div style="margin-bottom:20px;">
                            <h3 style="color:#333;margin:0 0 10px;font-size:14px;">
                                💬 Blocked Comment Content:
                            </h3>
                            <div style="background:#fff3cd;border:1px solid #ffc107;
                                        border-left:4px solid #f44336;border-radius:6px;
                                        padding:14px;font-size:13px;color:#555;
                                        font-style:italic;line-height:1.6;">
                                "%s"
                            </div>
                        </div>

                        <!-- Detected Words -->
                        <div style="margin-bottom:24px;">
                            <h3 style="color:#333;margin:0 0 10px;font-size:14px;">
                                🔍 Detected Inappropriate Words:
                            </h3>
                            <div style="padding:10px;background:#ffeaea;
                                        border-radius:6px;min-height:30px;">
                                %s
                            </div>
                        </div>

                        <!-- Status -->
                        <div style="background:#e8f5e9;border-radius:8px;padding:14px;
                                    text-align:center;margin-bottom:24px;">
                            <span style="color:#2e7d32;font-weight:bold;font-size:14px;">
                                ✅ Comment has been automatically BLOCKED
                            </span>
                            <p style="color:#388e3c;font-size:12px;margin:6px 0 0;">
                                No action required. The user has been notified.
                            </p>
                        </div>

                        <!-- Action Note -->
                        <div style="background:#e3f2fd;border-radius:8px;padding:14px;">
                            <p style="color:#1565c0;font-size:13px;margin:0;">
                                📋 <strong>Admin Note:</strong> 
                                If this is a repeated offense from the same user, 
                                consider reviewing their account activity.
                            </p>
                        </div>
                    </div>

                    <!-- Footer -->
                    <div style="background:#f9f9f9;padding:16px;text-align:center;
                                border-top:1px solid #eee;">
                        <p style="color:#999;font-size:12px;margin:0;">
                            This is an automated notification from %s Content Moderation System.
                        </p>
                        <p style="color:#999;font-size:11px;margin:6px 0 0;">
                            Do not reply to this email.
                        </p>
                    </div>
                    
                </div>
            </body>
            </html>
            """.formatted(
                appName,
                categoryColor,
                categoryEmoji,
                category.replace("_", " "),
                commentAuthor != null ? commentAuthor : "Anonymous",
                articleId != null ? articleId : "Unknown",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                commentContent != null ? commentContent : "",
                detectedWordsHtml.isEmpty() ? "<span style='color:#999'>None detected</span>" : detectedWordsHtml,
                appName
            );
    }
}
