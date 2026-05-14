package com.travelnest.notification.service;

import com.travelnest.common.exception.ServiceUnavailableException;
import com.travelnest.config.AppMailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    @Nullable
    private final JavaMailSender javaMailSender;
    private final AppMailProperties appMailProperties;
    private final String smtpHost;

    public SmtpEmailService(
            ObjectProvider<JavaMailSender> javaMailSenderProvider,
            AppMailProperties appMailProperties,
            @Value("${spring.mail.host:}") String smtpHost
    ) {
        this.javaMailSender = javaMailSenderProvider.getIfAvailable();
        this.appMailProperties = appMailProperties;
        this.smtpHost = smtpHost;
    }

    @Override
    public void sendResetPasswordOtp(String recipientEmail, String recipientName, String otp, int expiresInMinutes) {
        if (!appMailProperties.isEnabled()) {
            log.info("Skipping reset OTP email because app mail delivery is disabled");
            return;
        }

        validateConfiguration();

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(recipientEmail);
            helper.setFrom(appMailProperties.getFrom().trim());
            helper.setSubject("TravelNest password reset OTP");
            helper.setText(buildResetPasswordBody(recipientName, otp, expiresInMinutes), false);

            javaMailSender.send(message);
            log.info("Sent reset OTP email to {}", recipientEmail);
        } catch (MailException | MessagingException exception) {
            log.error("Failed to send reset OTP email to {}", recipientEmail, exception);
            throw new ServiceUnavailableException(buildDeliveryErrorMessage(exception));
        }
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(smtpHost) || !StringUtils.hasText(appMailProperties.getFrom())) {
            throw new ServiceUnavailableException("Email delivery is enabled but SMTP is not fully configured");
        }

        if (javaMailSender == null) {
            throw new ServiceUnavailableException("Email delivery is enabled but mail sender is unavailable");
        }
    }

    private String buildResetPasswordBody(String recipientName, String otp, int expiresInMinutes) {
        String greetingName = StringUtils.hasText(recipientName) ? recipientName.trim() : "there";

        return """
                Hello %s,

                We received a request to reset your TravelNest password.

                Your one-time password is: %s

                This OTP expires in %d minutes.

                If you did not request this, you can safely ignore this email.

                TravelNest
                """.formatted(greetingName, otp, expiresInMinutes);
    }

    private String buildDeliveryErrorMessage(Exception exception) {
        if (exception instanceof MailAuthenticationException) {
            if (smtpHost.toLowerCase().contains("gmail")) {
                return "Gmail SMTP authentication failed. Check username and use a valid Google App Password";
            }
            return "SMTP authentication failed. Check SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD";
        }

        String details = collectExceptionMessages(exception).toLowerCase();
        if (details.contains("timed out") || details.contains("timeout") || details.contains("could not connect")) {
            return "Cannot connect to the SMTP server. Check SPRING_MAIL_HOST, SPRING_MAIL_PORT, and network access";
        }

        if (details.contains("535") || details.contains("authentication failed")) {
            if (smtpHost.toLowerCase().contains("gmail")) {
                return "Gmail SMTP authentication failed. Check username and use a valid Google App Password";
            }
            return "SMTP authentication failed. Check SPRING_MAIL_USERNAME and SPRING_MAIL_PASSWORD";
        }

        return "Unable to send reset email right now. Check backend logs for the SMTP error details";
    }

    private String collectExceptionMessages(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            if (StringUtils.hasText(current.getMessage())) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(current.getMessage());
            }
            current = current.getCause();
        }
        return builder.toString();
    }
}
