package com.ishan.emailclientapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.mail")
public class MailProperties {
    private String host;
    private int port;
    private String username;
    private String password;
    
    // Connection pool settings
    private Pool pool = new Pool();
    
    // Multiple SMTP servers support
    private List<SmtpServer> servers;
    
    // Default SMTP server (for backward compatibility)
    private SmtpServer defaultServer;
    
    @Getter
    @Setter
    public static class Pool {
        private int maxTotal = 20;
        private int maxIdle = 10;
        private int minIdle = 5;
        private long maxWaitMillis = 30000;
        private long timeBetweenEvictionRunsMillis = 60000;
        private long minEvictableIdleTimeMillis = 300000;
        private boolean testOnBorrow = true;
        private boolean testOnReturn = false;
        private boolean testWhileIdle = true;
        private int maxConnectionsPerServer = 5;
    }
    
    @Getter
    @Setter
    public static class SmtpServer {
        private String name;
        private String host;
        private int port;
        private String username;
        private String password;
        private boolean enabled = true;
        private int weight = 1; // Load balancing weight
        private boolean ssl = false;
        private boolean startTls = true;
        private int connectionTimeout = 30000;
        private int readTimeout = 30000;
    }
}
