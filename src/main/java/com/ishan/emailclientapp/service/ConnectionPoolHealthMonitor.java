package com.ishan.emailclientapp.service;

import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.mail.Transport;
import java.util.HashMap;
import java.util.Map;

@Service
public class ConnectionPoolHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionPoolHealthMonitor.class);

    @Autowired
    private ObjectPool<Transport> smtpConnectionPool;

    /**
     * Monitor connection pool health every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    public void monitorPoolHealth() {
        try {
            Map<String, Object> stats = getPoolStatistics();
            
            // Log pool statistics
            logger.info("Connection Pool Health Check - Active: {}, Idle: {}, Total: {}", 
                stats.get("numActive"),
                stats.get("numIdle"),
                stats.get("numTotal"));
            
            // Alert if pool is getting full
            int activeConnections = (Integer) stats.get("numActive");
            int maxTotal = (Integer) stats.get("maxTotal");
            
            if (activeConnections > maxTotal * 0.8) {
                logger.warn("Connection pool is 80% full! Active: {}, Max: {}", activeConnections, maxTotal);
            }
            
            // Alert if no idle connections
            int idleConnections = (Integer) stats.get("numIdle");
            if (idleConnections == 0) {
                logger.warn("No idle connections available in the pool");
            }
            
        } catch (Exception e) {
            logger.error("Error monitoring connection pool health", e);
        }
    }

    /**
     * Get comprehensive pool statistics
     */
    public Map<String, Object> getPoolStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            stats.put("numActive", smtpConnectionPool.getNumActive());
            stats.put("numIdle", smtpConnectionPool.getNumIdle());
            stats.put("numTotal", smtpConnectionPool.getNumActive() + smtpConnectionPool.getNumIdle());
            
            // Set default values for methods not available on ObjectPool interface
            stats.put("maxTotal", 20); // Default from MailProperties
            stats.put("maxIdle", 10);  // Default from MailProperties
            stats.put("minIdle", 5);   // Default from MailProperties
            
        } catch (Exception e) {
            logger.error("Error getting pool statistics", e);
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    /**
     * Test pool connectivity by borrowing and returning a connection
     */
    public boolean testPoolConnectivity() {
        Transport transport = null;
        try {
            transport = smtpConnectionPool.borrowObject();
            boolean isConnected = transport.isConnected();
            logger.info("Pool connectivity test: {}", isConnected ? "SUCCESS" : "FAILED");
            return isConnected;
        } catch (Exception e) {
            logger.error("Pool connectivity test failed", e);
            return false;
        } finally {
            if (transport != null) {
                try {
                    smtpConnectionPool.returnObject(transport);
                } catch (Exception e) {
                    logger.warn("Failed to return test transport to pool", e);
                }
            }
        }
    }

    /**
     * Get pool health status
     */
    public String getPoolHealthStatus() {
        try {
            Map<String, Object> stats = getPoolStatistics();
            int active = (Integer) stats.get("numActive");
            int maxTotal = (Integer) stats.get("maxTotal");
            int idle = (Integer) stats.get("numIdle");
            
            if (active >= maxTotal) {
                return "CRITICAL - Pool is full";
            } else if (active > maxTotal * 0.8) {
                return "WARNING - Pool is 80% full";
            } else if (idle == 0) {
                return "WARNING - No idle connections";
            } else {
                return "HEALTHY - Pool operating normally";
            }
        } catch (Exception e) {
            return "ERROR - Unable to determine pool health";
        }
    }
}
