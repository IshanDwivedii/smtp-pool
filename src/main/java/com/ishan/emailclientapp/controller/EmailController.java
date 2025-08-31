package com.ishan.emailclientapp.controller;

import com.ishan.emailclientapp.model.EmailRequest;
import com.ishan.emailclientapp.model.EmailResponse;
import com.ishan.emailclientapp.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

@RestController
@RequestMapping("/api/email")
public class EmailController {

    private static final Logger logger = LoggerFactory.getLogger(EmailController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@RequestBody EmailRequest emailRequest) {
        logger.info("Received email request: {}", emailRequest);
        
        boolean success = emailService.sendEmail(emailRequest);
        
        EmailResponse response = new EmailResponse();
        response.setSuccess(success);
        response.setMessage(success ? "Email sent successfully" : "Failed to send email");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-async")
    public ResponseEntity<CompletableFuture<EmailResponse>> sendEmailAsync(@RequestBody EmailRequest emailRequest) {
        logger.info("Received async email request: {}", emailRequest);
        
        CompletableFuture<EmailResponse> futureResponse = emailService.sendEmailAsync(emailRequest)
                .thenApply(success -> {
                    EmailResponse response = new EmailResponse();
                    response.setSuccess(success);
                    response.setMessage(success ? "Email sent successfully" : "Failed to send email");
                    return response;
                });
        
        return ResponseEntity.ok(futureResponse);
    }
    
    @PostMapping("/send-bulk")
    public ResponseEntity<EmailResponse> sendBulkEmails(@RequestBody List<EmailRequest> emailRequests) {
        logger.info("Received bulk email request for {} emails", emailRequests.size());
        
        boolean success = emailService.sendBulkEmails(emailRequests);
        
        EmailResponse response = new EmailResponse();
        response.setSuccess(success);
        response.setMessage(success ? 
            String.format("Bulk email send completed for %d emails", emailRequests.size()) :
            "Failed to send some or all emails in bulk");
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/send-legacy")
    public ResponseEntity<EmailResponse> sendEmailLegacy(@RequestBody EmailRequest emailRequest) {
        logger.info("Received legacy email request: {}", emailRequest);
        
        boolean success = emailService.sendEmailLegacy(emailRequest);
        
        EmailResponse response = new EmailResponse();
        response.setSuccess(success);
        response.setMessage(success ? "Email sent successfully using legacy method" : "Failed to send email using legacy method");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pool/stats")
    public ResponseEntity<Map<String, String>> getPoolStats() {
        String stats = emailService.getPoolStats();
        return ResponseEntity.ok(Map.of("poolStats", stats));
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "SMTP Pool Client"));
    }
    
    @GetMapping("/debug/config")
    public ResponseEntity<Map<String, Object>> debugConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", emailService.getMailProperties().getHost());
        config.put("port", emailService.getMailProperties().getPort());
        config.put("username", emailService.getMailProperties().getUsername());
        config.put("poolMaxTotal", emailService.getMailProperties().getPool().getMaxTotal());
        config.put("poolMaxIdle", emailService.getMailProperties().getPool().getMaxIdle());
        config.put("poolMinIdle", emailService.getMailProperties().getPool().getMinIdle());
        return ResponseEntity.ok(config);
    }
}
