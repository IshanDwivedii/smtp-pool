package com.ishan.emailclientapp.service;

import com.ishan.emailclientapp.model.EmailRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    public JavaMailSender mailSender;

    public boolean sendEmail(EmailRequest emailRequest) {
        // Check if emailRequest is null
        if (emailRequest == null) {
            logger.error("Email request is null.");
            return false; // Indicate failure
        }

        // Validate fields in emailRequest
        if (emailRequest.getFrom() == null || emailRequest.getFrom().isEmpty() ||
                emailRequest.getTo() == null || emailRequest.getTo().isEmpty() ||
                emailRequest.getSubject() == null || emailRequest.getSubject().isEmpty() ||
                emailRequest.getBody() == null || emailRequest.getBody().isEmpty()) {
            logger.error("Invalid email request parameters.");
            return false; // Indicate failure
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailRequest.getFrom());
            message.setTo(emailRequest.getTo().toArray(new String[0])); // Convert List to Array
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getBody());

            mailSender.send(message);
            logger.info("Email sent successfully to {}", emailRequest.getTo());
            return true; // Indicate success
        } catch (MailException e) {
            logger.error("Failed to send email", e);
            return false; // Indicate failure.
        }
    }
}
