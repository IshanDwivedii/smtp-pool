# SMTP Pool Client Application

A high-performance Spring Boot application that implements SMTP connection pooling for efficient email delivery with connection reuse capabilities.

## 🚀 Features

### Core Functionality
- **SMTP Connection Pooling**: Reuses SMTP connections for improved performance
- **Multiple SMTP Server Support**: Load balancing across multiple email providers
- **Async Email Processing**: Non-blocking email sending with CompletableFuture
- **Bulk Email Support**: Efficiently send multiple emails using pooled connections
- **Connection Health Monitoring**: Real-time monitoring of pool health and statistics

### Performance Benefits
- **Reduced Connection Overhead**: Eliminates repeated SMTP handshakes
- **Improved Throughput**: Parallel email processing with connection reuse
- **Better Resource Management**: Efficient connection lifecycle management
- **Load Balancing**: Round-robin distribution across multiple SMTP servers

## 🏗️ Architecture

### Connection Pool Implementation
- **Apache Commons Pool2**: Industry-standard connection pooling framework
- **Jakarta Mail API**: Modern Java mail implementation
- **Spring Boot Integration**: Seamless integration with Spring ecosystem

### Key Components
- `EmailConfig`: SMTP connection pool configuration and factory
- `EmailService`: Pooled email sending service with async support
- `ConnectionPoolHealthMonitor`: Real-time pool health monitoring
- `MailProperties`: Configurable pool and server settings

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- SMTP server credentials (Gmail, Outlook, etc.)

## 🛠️ Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd smtp-pool
```

### 2. Configure SMTP Settings
Edit `src/main/resources/application.yml`:

```yaml
spring:
  mail:
    username: ${SMTP_USERNAME:your-email@gmail.com}
    password: ${SMTP_PASSWORD:your-app-password}
    
    # Connection Pool Settings
    pool:
      maxTotal: 20
      maxIdle: 10
      minIdle: 5
      maxWaitMillis: 30000
      
    # Multiple SMTP Servers
    servers:
      - name: gmail-primary
        host: smtp.gmail.com
        port: 587
        username: ${SMTP_USERNAME}
        password: ${SMTP_PASSWORD}
        enabled: true
        weight: 2
```

### 3. Set Environment Variables
```bash
export SMTP_USERNAME="your-email@gmail.com"
export SMTP_PASSWORD="your-app-password"
```

### 4. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

## 📡 API Endpoints

### Email Operations
- `POST /api/email/send` - Send single email using pooled connection
- `POST /api/email/send-async` - Send email asynchronously
- `POST /api/email/send-bulk` - Send multiple emails in bulk
- `POST /api/email/send-legacy` - Send email using legacy method

### Monitoring & Health
- `GET /api/email/pool/stats` - Get connection pool statistics
- `GET /api/email/health` - Service health check
- `GET /actuator/health` - Detailed health information
- `GET /actuator/metrics` - Application metrics

## 🔧 Configuration Options

### Connection Pool Settings
| Property | Default | Description |
|----------|---------|-------------|
| `maxTotal` | 20 | Maximum total connections |
| `maxIdle` | 10 | Maximum idle connections |
| `minIdle` | 5 | Minimum idle connections |
| `maxWaitMillis` | 30000 | Max wait time for connection (ms) |
| `testOnBorrow` | true | Test connection when borrowing |
| `testWhileIdle` | true | Test idle connections |

### SMTP Server Configuration
| Property | Description |
|----------|-------------|
| `name` | Unique server identifier |
| `host` | SMTP server hostname |
| `port` | SMTP server port |
| `weight` | Load balancing weight |
| `enabled` | Enable/disable server |
| `ssl` | Use SSL encryption |
| `startTls` | Enable STARTTLS |

## 📊 Performance Monitoring

### Pool Statistics
The application provides real-time connection pool metrics:
- Active connections
- Idle connections
- Total connections
- Connection creation/destruction rates

### Health Checks
- Connection pool health status
- SMTP server connectivity
- Pool utilization warnings
- Performance alerts

## 🚀 Usage Examples

### Send Single Email
```bash
curl -X POST http://localhost:8080/api/email/send \
  -H "Content-Type: application/json" \
  -d '{
    "from": "sender@example.com",
    "to": ["recipient@example.com"],
    "subject": "Test Email",
    "body": "Hello from SMTP Pool Client!"
  }'
```

### Send Bulk Emails
```bash
curl -X POST http://localhost:8080/api/email/send-bulk \
  -H "Content-Type: application/json" \
  -d '[
    {
      "from": "sender@example.com",
      "to": ["user1@example.com"],
      "subject": "Bulk Email 1",
      "body": "First email"
    },
    {
      "from": "sender@example.com",
      "to": ["user2@example.com"],
      "subject": "Bulk Email 2",
      "body": "Second email"
    }
  ]'
```

### Get Pool Statistics
```bash
curl http://localhost:8080/api/email/pool/stats
```

## 🔍 Troubleshooting

### Common Issues

1. **Connection Pool Full**
   - Increase `maxTotal` in configuration
   - Check for connection leaks
   - Monitor pool statistics

2. **SMTP Authentication Failed**
   - Verify credentials in configuration
   - Check SMTP server settings
   - Enable debug logging

3. **Connection Timeouts**
   - Adjust `connectionTimeout` and `readTimeout`
   - Check network connectivity
   - Verify firewall settings

### Debug Mode
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.ishan.emailclientapp: DEBUG
    org.apache.commons.pool2: DEBUG
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Integration Testing
```bash
mvn test -Dtest=EmailServiceIntegrationTest
```

## 📈 Performance Tuning

### Optimal Pool Sizes
- **Small Applications**: `maxTotal: 10, maxIdle: 5`
- **Medium Applications**: `maxTotal: 20, maxIdle: 10`
- **High-Volume Applications**: `maxTotal: 50, maxIdle: 20`

### Connection Validation
- Enable `testOnBorrow` for critical applications
- Use `testWhileIdle` for long-running applications
- Adjust eviction intervals based on usage patterns

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🆘 Support

For support and questions:
- Create an issue in the repository
- Check the troubleshooting section
- Review the configuration examples

---

**Built with ❤️ using Spring Boot and Apache Commons Pool2**
