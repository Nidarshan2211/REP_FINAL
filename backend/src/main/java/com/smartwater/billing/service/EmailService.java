package com.smartwater.billing.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public boolean sendResidentInvitation(String toEmail, String residentName, String ownerName, String buildingName, String apartmentNumber, String rawToken) {
        String encodedEmail = URLEncoder.encode(toEmail != null ? toEmail.trim() : "", StandardCharsets.UTF_8);
        String inviteLink = frontendUrl + "/accept-invitation?token=" + rawToken + "&email=" + encodedEmail;
        String subject = "You're Invited to AquaFlow - Activate Your Resident Account";

        String senderInfo = ownerName != null && !ownerName.isEmpty() ? ownerName : "your Building Owner";

        String message = String.format(
                "Hello %s,\n\n" +
                "You have been invited by %s to join AquaFlow as a resident of:\n\n" +
                "%s (Apartment %s)\n\n" +
                "Your resident account has been created.\n\n" +
                "Click the link below to activate your account:\n" +
                "%s\n\n" +
                "This link will expire in 24 hours and can only be used once.\n\n" +
                "If you did not expect this invitation, you can safely ignore this email.\n\n" +
                "Regards,\n" +
                "AquaFlow Team\n" +
                "Smart Water Usage and Consumer Billing System",
                residentName, senderInfo, buildingName, apartmentNumber, inviteLink
        );

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);
            log.info("Invitation email successfully sent to: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.warn("Could not send real email to {}: {}. Invitation URL: {}", toEmail, e.getMessage(), inviteLink);
            return false;
        }
    }

    public void sendNotificationEmail(String toEmail, String residentName, String subject, String content) {
        String message = String.format(
                "Dear %s,\n\n" +
                "%s\n\n" +
                "Log into your AquaFlow portal at %s/login to view complete billing & usage details.\n\n" +
                "Best regards,\n" +
                "AquaFlow Team",
                residentName, content, frontendUrl
        );

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(toEmail);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailSender.send(mailMessage);
            log.info("Notification email dispatched to: {}", toEmail);
        } catch (Exception e) {
            log.warn("Could not send notification email to {}: {}", toEmail, e.getMessage());
        }
    }
}
