package com.ishan.emailclientapp.service;

import com.ishan.emailclientapp.model.EmailRequest;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender; // Kept for backward compatibility
    
    @Autowired
    private ObjectPool<Transport> smtpConnectionPool;
    
    @Autowired
    private com.ishan.emailclientapp.config.MailProperties mailProperties;
    
    private final ExecutorService emailExecutor = Executors.newFixedThreadPool(10);

    /**
     * Send email using the pooled SMTP connections for better performance
     */
    public boolean sendEmail(EmailRequest emailRequest) {
        // Check if emailRequest is null
        if (emailRequest == null) {
            logger.error("Email request is null.");
            return false;
        }

        // Validate fields in emailRequest
        if (emailRequest.getFrom() == null || emailRequest.getFrom().isEmpty() ||
                emailRequest.getTo() == null || emailRequest.getTo().isEmpty() ||
                emailRequest.getSubject() == null || emailRequest.getSubject().isEmpty() ||
                emailRequest.getBody() == null || emailRequest.getBody().isEmpty()) {
            logger.error("Invalid email request parameters.");
            return false;
        }

        Transport transport = null;
        try {
            // Borrow connection from pool
            transport = smtpConnectionPool.borrowObject();
            
            // Create and send message using pooled connection
            boolean success = sendEmailWithTransport(transport, emailRequest);
            
            if (success) {
                logger.info("Email sent successfully to {} using pooled connection", emailRequest.getTo());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Failed to send email using pooled connection", e);
            return false;
        } finally {
            // Always return connection to pool
            if (transport != null) {
                try {
                    smtpConnectionPool.returnObject(transport);
                } catch (Exception e) {
                    logger.warn("Failed to return transport to pool", e);
                    try {
                        smtpConnectionPool.invalidateObject(transport);
                    } catch (Exception invalidateEx) {
                        logger.error("Failed to invalidate transport", invalidateEx);
                    }
                }
            }
        }
    }
    
    /**
     * Send email asynchronously using pooled connections
     */
    public CompletableFuture<Boolean> sendEmailAsync(EmailRequest emailRequest) {
        return CompletableFuture.supplyAsync(() -> sendEmail(emailRequest), emailExecutor);
    }
    
    /**
     * Send multiple emails using pooled connections
     */
    public boolean sendBulkEmails(java.util.List<EmailRequest> emailRequests) {
        if (emailRequests == null || emailRequests.isEmpty()) {
            logger.warn("No email requests provided for bulk sending");
            return false;
        }
        
        logger.info("Starting bulk email send for {} emails", emailRequests.size());
        
        // Process emails in parallel using the connection pool
        java.util.List<CompletableFuture<Boolean>> futures = emailRequests.stream()
                .map(this::sendEmailAsync)
                .toList();
        
        // Wait for all emails to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // Count successful sends
        long successCount = futures.stream()
                .mapToLong(future -> {
                    try {
                        return future.get() ? 1 : 0;
                    } catch (Exception e) {
                        logger.error("Error getting future result", e);
                        return 0;
                    }
                })
                .sum();
        
        logger.info("Bulk email send completed. Success: {}/{}", successCount, emailRequests.size());
        return successCount == emailRequests.size();
    }
    
    /**
     * Send email using a specific Transport connection
     */
    private boolean sendEmailWithTransport(Transport transport, EmailRequest emailRequest) {
        try {
            // Create a new session for this message
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            
            Session session = Session.getInstance(props, null);
            MimeMessage message = new MimeMessage(session);
            
            message.setFrom(new InternetAddress(emailRequest.getFrom()));
            for (String to : emailRequest.getTo()) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            }
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getBody());
            
            // Send using the pooled transport
            transport.sendMessage(message, message.getAllRecipients());
            
            return true;
            
        } catch (Exception e) {
            logger.error("Failed to send email with transport", e);
            return false;
        }
    }
    
    /**
     * Legacy method using JavaMailSender (kept for backward compatibility)
     */
    public boolean sendEmailLegacy(EmailRequest emailRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailRequest.getFrom());
            message.setTo(emailRequest.getTo().toArray(new String[0]));
            message.setSubject(emailRequest.getSubject());
            message.setText(emailRequest.getBody());

            mailSender.send(message);
            logger.info("Email sent successfully using legacy method to {}", emailRequest.getTo());
            return true;
        } catch (MailException e) {
            logger.error("Failed to send email using legacy method", e);
            return false;
        }
    }
    
    /**
     * Get connection pool statistics
     */
    public String getPoolStats() {
        try {
            return String.format("Pool Stats - Active: %d, Idle: %d, Total: %d", 
                smtpConnectionPool.getNumActive(),
                smtpConnectionPool.getNumIdle(),
                smtpConnectionPool.getNumActive() + smtpConnectionPool.getNumIdle());
        } catch (Exception e) {
            return "Unable to get pool stats: " + e.getMessage();
        }
    }
    
    /**
     * Get mail properties for debugging
     */
    public com.ishan.emailclientapp.config.MailProperties getMailProperties() {
        return mailProperties;
    }
}
