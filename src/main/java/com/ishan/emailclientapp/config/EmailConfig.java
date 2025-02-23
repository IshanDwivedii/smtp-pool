package com.ishan.emailclientapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;

@Configuration
public class EmailConfig {
    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);

    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        logger.info("Configuring JavaMailSender with:");
        logger.info("Host: {}", mailSender.getHost());
        logger.info("Port: {}", mailSender.getPort());
        logger.info("Username: {}", mailSender.getUsername());
        logger.info("Password: [PROTECTED]"); // NEVER log

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }
}
