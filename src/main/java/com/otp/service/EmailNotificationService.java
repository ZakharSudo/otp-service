package com.otp.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.Properties;

@Service
public class EmailNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
    private final String username;
    private final String password;
    private final String fromEmail;
    private final Session session;
    private boolean configured = false;
    
    public EmailNotificationService() {
        Properties config = loadConfig();
        this.username = config.getProperty("email.username");
        this.password = config.getProperty("email.password");
        this.fromEmail = config.getProperty("email.from");
        
        if (username != null && !username.equals("your_email@gmail.com") && !username.isEmpty()) {
            Properties mailProps = new Properties();
            mailProps.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
            mailProps.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
            mailProps.put("mail.smtp.auth", config.getProperty("mail.smtp.auth"));
            mailProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable"));
            
            this.session = Session.getInstance(mailProps, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            this.configured = true;
            logger.info("Email notification service configured for: {}", username);
        } else {
            this.session = null;
            logger.warn("Email notification service not configured - please update email.properties");
        }
    }
    
    private Properties loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            Properties props = new Properties();
            if (input != null) {
                props.load(input);
            } else {
                logger.warn("email.properties not found, using defaults");
                props.setProperty("email.username", "");
                props.setProperty("email.password", "");
                props.setProperty("email.from", "");
                props.setProperty("mail.smtp.host", "smtp.gmail.com");
                props.setProperty("mail.smtp.port", "587");
                props.setProperty("mail.smtp.auth", "true");
                props.setProperty("mail.smtp.starttls.enable", "true");
            }
            return props;
        } catch (Exception e) {
            logger.error("Failed to load email configuration", e);
            throw new RuntimeException("Failed to load email configuration", e);
        }
    }
    
    public void sendCode(String toEmail, String code) {
        if (!configured) {
            logger.warn("Email not configured - would send code {} to {}", code, toEmail);
            return;
        }
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("Your OTP Verification Code");
            message.setText(String.format("Your verification code is: %s\n\nThis code will expire in 5 minutes.\n\nIf you didn't request this, please ignore this message.", code));
            
            Transport.send(message);
            logger.info("OTP code sent to email: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    public boolean isConfigured() { return configured; }
}