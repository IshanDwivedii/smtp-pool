package com.ishan.emailclientapp.config;

import com.ishan.emailclientapp.config.MailProperties.SmtpServer;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.mail.Session;
import jakarta.mail.Transport;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class EmailConfig {
    private static final Logger logger = LoggerFactory.getLogger(EmailConfig.class);
    
    @Autowired
    private MailProperties mailProperties;

    @Bean
    public GenericObjectPool<Transport> smtpConnectionPool() {
        GenericObjectPoolConfig<Transport> config = new GenericObjectPoolConfig<>();
        
        config.setMaxTotal(mailProperties.getPool().getMaxTotal());
        config.setMaxIdle(mailProperties.getPool().getMaxIdle());
        config.setMinIdle(mailProperties.getPool().getMinIdle());
        config.setMaxWaitMillis(mailProperties.getPool().getMaxWaitMillis());
        config.setTimeBetweenEvictionRunsMillis(mailProperties.getPool().getTimeBetweenEvictionRunsMillis());
        config.setMinEvictableIdleTimeMillis(mailProperties.getPool().getMinEvictableIdleTimeMillis());
        config.setTestOnBorrow(mailProperties.getPool().isTestOnBorrow());
        config.setTestOnReturn(mailProperties.getPool().isTestOnReturn());
        config.setTestWhileIdle(mailProperties.getPool().isTestWhileIdle());
        
        // Create pool factory based on available servers
        List<SmtpServer> servers = mailProperties.getServers();
        if (servers == null || servers.isEmpty()) {
            // Fallback to default configuration
            servers = List.of(createDefaultSmtpServer());
        }
        
        SmtpConnectionPoolFactory factory = new SmtpConnectionPoolFactory(servers);
        
        GenericObjectPool<Transport> pool = new GenericObjectPool<>(factory, config);
        
        logger.info("SMTP Connection Pool initialized with {} max connections", config.getMaxTotal());
        logger.info("Available SMTP servers: {}", servers.stream().map(SmtpServer::getName).toList());
        
        return pool;
    }
    
    private SmtpServer createDefaultSmtpServer() {
        SmtpServer server = new SmtpServer();
        server.setName("default");
        server.setHost(mailProperties.getHost());
        server.setPort(mailProperties.getPort());
        server.setUsername(mailProperties.getUsername());
        server.setPassword(mailProperties.getPassword());
        server.setStartTls(true);
        return server;
    }
    
    @Bean
    public JavaMailSender javaMailSender() {
        // This bean is kept for backward compatibility
        // The actual email sending will use the connection pool
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        if (mailProperties.getServers() != null && !mailProperties.getServers().isEmpty()) {
            SmtpServer firstServer = mailProperties.getServers().get(0);
            mailSender.setHost(firstServer.getHost());
            mailSender.setPort(firstServer.getPort());
            mailSender.setUsername(firstServer.getUsername());
            mailSender.setPassword(firstServer.getPassword());
        } else {
            mailSender.setHost(mailProperties.getHost());
            mailSender.setPort(mailProperties.getPort());
            mailSender.setUsername(mailProperties.getUsername());
            mailSender.setPassword(mailProperties.getPassword());
        }
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "false"); // Set to false for production
        
        return mailSender;
    }
    
    /**
     * Factory for creating SMTP Transport connections
     */
    public static class SmtpConnectionPoolFactory extends BasePooledObjectFactory<Transport> {
        private final List<SmtpServer> servers;
        private final AtomicInteger serverIndex = new AtomicInteger(0);
        
        public SmtpConnectionPoolFactory(List<SmtpServer> servers) {
            this.servers = servers;
        }
        
        @Override
        public Transport create() throws Exception {
            // Round-robin server selection for load balancing
            SmtpServer server = getNextServer();
            
            Properties props = new Properties();
            props.put("mail.smtp.host", server.getHost());
            props.put("mail.smtp.port", server.getPort());
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", server.isStartTls());
            props.put("mail.smtp.ssl.enable", server.isSsl());
            props.put("mail.smtp.connectiontimeout", server.getConnectionTimeout());
            props.put("mail.smtp.timeout", server.getReadTimeout());
            props.put("mail.smtp.writetimeout", server.getReadTimeout());
            
            Session session = Session.getInstance(props, null);
            Transport transport = session.getTransport("smtp");
            
            // Don't connect immediately - let the pool handle connection lifecycle
            logger.info("Created new SMTP transport for {}:{} (not connected yet)", server.getHost(), server.getPort());
            return transport;
        }
        
        @Override
        public PooledObject<Transport> wrap(Transport transport) {
            return new DefaultPooledObject<>(transport);
        }
        
        @Override
        public boolean validateObject(PooledObject<Transport> pooledObject) {
            try {
                Transport transport = pooledObject.getObject();
                // If not connected, try to connect
                if (!transport.isConnected()) {
                    SmtpServer server = getNextServer();
                    transport.connect(server.getHost(), server.getPort(), server.getUsername(), server.getPassword());
                }
                return transport.isConnected();
            } catch (Exception e) {
                logger.warn("SMTP connection validation failed", e);
                return false;
            }
        }
        
        @Override
        public void destroyObject(PooledObject<Transport> pooledObject) throws Exception {
            try {
                Transport transport = pooledObject.getObject();
                if (transport.isConnected()) {
                    transport.close();
                }
                logger.debug("SMTP connection destroyed");
            } catch (Exception e) {
                logger.warn("Error destroying SMTP connection", e);
            }
        }
        
        private SmtpServer getNextServer() {
            int index = serverIndex.getAndIncrement() % servers.size();
            return servers.get(index);
        }
    }
}
