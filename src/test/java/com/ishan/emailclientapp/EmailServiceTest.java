package com.ishan.emailclientapp;

import com.ishan.emailclientapp.model.EmailRequest;
import com.ishan.emailclientapp.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.mail.host=smtp.gmail.com",
    "spring.mail.port=587",
    "spring.mail.username=test@example.com",
    "spring.mail.password=test-password",
    "spring.mail.pool.maxTotal=5",
    "spring.mail.pool.maxIdle=2",
    "spring.mail.pool.minIdle=1"
})
public class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testEmailServiceInitialization() {
        assertNotNull(emailService, "EmailService should be initialized");
    }

    @Test
    public void testPoolStats() {
        String stats = emailService.getPoolStats();
        assertNotNull(stats, "Pool stats should not be null");
        assertTrue(stats.contains("Pool Stats"), "Pool stats should contain pool information");
    }

    @Test
    public void testEmailRequestValidation() {
        // Test null request
        EmailRequest nullRequest = null;
        assertFalse(emailService.sendEmail(nullRequest), "Should return false for null request");

        // Test empty request
        EmailRequest emptyRequest = new EmailRequest();
        assertFalse(emailService.sendEmail(emptyRequest), "Should return false for empty request");

        // Test valid request (will fail due to invalid SMTP credentials, but should not throw exception)
        EmailRequest validRequest = new EmailRequest();
        validRequest.setFrom("test@example.com");
        validRequest.setTo(Arrays.asList("recipient@example.com"));
        validRequest.setSubject("Test Subject");
        validRequest.setBody("Test Body");
        
        // This will fail due to invalid SMTP credentials, but should not throw exception
        try {
            boolean result = emailService.sendEmail(validRequest);
            // Result will be false due to invalid credentials, but that's expected in test environment
            assertFalse(result, "Should return false for invalid SMTP credentials in test environment");
        } catch (Exception e) {
            fail("EmailService should handle SMTP errors gracefully without throwing exceptions");
        }
    }

    @Test
    public void testBulkEmailValidation() {
        // Test null list
        List<EmailRequest> nullList = null;
        assertFalse(emailService.sendBulkEmails(nullList), "Should return false for null list");

        // Test empty list
        List<EmailRequest> emptyList = Arrays.asList();
        assertFalse(emailService.sendBulkEmails(emptyList), "Should return false for empty list");

        // Test valid list (will fail due to invalid SMTP credentials, but should not throw exception)
        EmailRequest request1 = new EmailRequest();
        request1.setFrom("test@example.com");
        request1.setTo(Arrays.asList("user1@example.com"));
        request1.setSubject("Test 1");
        request1.setBody("Body 1");

        EmailRequest request2 = new EmailRequest();
        request2.setFrom("test@example.com");
        request2.setTo(Arrays.asList("user2@example.com"));
        request2.setSubject("Test 2");
        request2.setBody("Body 2");

        List<EmailRequest> validList = Arrays.asList(request1, request2);
        
        try {
            boolean result = emailService.sendBulkEmails(validList);
            // Result will be false due to invalid SMTP credentials, but that's expected in test environment
            assertFalse(result, "Should return false for invalid SMTP credentials in test environment");
        } catch (Exception e) {
            fail("EmailService should handle SMTP errors gracefully without throwing exceptions");
        }
    }

    @Test
    public void testLegacyEmailMethod() {
        EmailRequest request = new EmailRequest();
        request.setFrom("test@example.com");
        request.setTo(Arrays.asList("recipient@example.com"));
        request.setSubject("Test Subject");
        request.setBody("Test Body");
        
        try {
            boolean result = emailService.sendEmailLegacy(request);
            // Result will be false due to invalid SMTP credentials, but that's expected in test environment
            assertFalse(result, "Should return false for invalid SMTP credentials in test environment");
        } catch (Exception e) {
            fail("Legacy email method should handle SMTP errors gracefully without throwing exceptions");
        }
    }
}
