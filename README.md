# Latency-Aware Retry Budget System

A sophisticated retry budget allocation system that dynamically adjusts retry budgets based on service latency, error rates, and overall health. This prevents cascading failures and retry storms in distributed systems.

## 🌟 Key Features

- **Dynamic Budget Allocation**: Automatically adjusts retry budgets based on service health
- **Latency-Aware**: Uses P95/P99 latency percentiles to determine service health
- **Error Rate Monitoring**: Tracks error rates and adjusts budgets accordingly
- **Real-Time Dashboard**: Beautiful web UI showing live service metrics and logs
- **WebSocket Updates**: Real-time push notifications of service status changes
- **Redis-backed**: Persistent state with automatic expiration (TTL)
- **Prometheus Metrics**: Full observability with Prometheus integration
- **Demo Services**: Built-in mock services to demonstrate the system

## 🎯 Use Cases

- Prevent retry storms in microservices architectures
- Protect degraded services from additional load
- Maintain system stability during partial outages
- Implement intelligent retry logic based on service health
- Monitor and visualize retry budget consumption in real-time

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for Redis)

### Run the Demo

```bash
# 1. Start Redis
docker-compose up -d

# 2. Build and run
./start-demo.sh

# OR manually:
mvn clean install -DskipTests
mvn spring-boot:run
```

### Access the Dashboard

Open your browser and navigate to:
```
http://localhost:8080
```

## 📖 Demo Guide

See [DEMO.md](DEMO.md) for a comprehensive guide on:
- How to use the demo dashboard
- Understanding the metrics
- Testing different scenarios
- API endpoints reference
- Troubleshooting

## 🏗️ Architecture

### Core Components

1. **Retry Budget Controller Service**
   - Calculates optimal retry budgets
   - Monitors service health
   - Adapts budgets dynamically

2. **Budget Enforcement Agent**
   - Enforces retry budget policies
   - Makes retry allow/deny decisions
   - Prevents budget violations

3. **Metrics Aggregator**
   - Collects request metrics
   - Calculates latency percentiles
   - Tracks error rates

4. **Latency Analyzer**
   - Statistical analysis of latency data
   - P50, P95, P99 percentile calculations
   - Uses Apache Commons Math

### Demo Components

1. **Demo Service Controller**
   - Simulates 4 services with different health profiles
   - Realistic latency and error rate simulation

2. **Demo Client Service**
   - Makes requests with retry budget awareness
   - Records detailed logs for dashboard display

3. **WebSocket Broadcast Service**
   - Real-time updates to connected clients
   - Broadcasts every 1 second

4. **Frontend Dashboard**
   - Real-time service health visualization
   - Request logs table
   - Interactive testing controls

## 🔧 Configuration

Edit `src/main/resources/application.yml`:

```yaml
retry:
  budget:
    base-budget: 5  # Base number of retries
    evaluation-window-seconds: 60
    update-interval-seconds: 10

    latency-thresholds:
      healthy-p95-ms: 200    # P95 < 200ms = HEALTHY
      degraded-p95-ms: 300   # P95 > 300ms = DEGRADED
      critical-p99-ms: 800   # P99 > 800ms = CRITICAL

    error-thresholds:
      warning-percent: 5     # Error rate > 5% = WARNING
      critical-percent: 10   # Error rate > 10% = CRITICAL
```

## 📊 How It Works

### Budget Calculation Formula

```
Budget = BaseBudget × LatencyFactor × ErrorFactor
```

- **LatencyFactor**: 0.0 - 1.0 (based on service latency percentiles)
- **ErrorFactor**: 0.0 - 1.0 (based on error rate)

### Health Status Determination

- **HEALTHY**: P95 latency < 200ms AND error rate < 5%
- **DEGRADED**: P95 latency 200-800ms OR error rate 5-10%
- **CRITICAL**: P99 latency > 800ms OR error rate > 10%

### Retry Decision Logic

```java
if (service.isCritical()) {
    return DENY;  // Protect critical services
}

if (budget.isExhausted()) {
    return DENY;  // Prevent retry storms
}

if (service.isDegraded()) {
    return DEFER;  // Backoff and retry cautiously
}

return ALLOW;  // Healthy service, retry freely
```

## 🛠️ API Reference

### Budget Management

```bash
# Get budget for a service
GET /api/budget/{serviceName}

# Calculate new budget
POST /api/budget/calculate/{serviceName}

# Check if retry is allowed
GET /api/budget/check/{serviceName}

# Consume budget (record retry)
POST /api/budget/consume/{serviceName}

# Update all budgets
POST /api/budget/update-all
```

### Metrics

```bash
# Get service metrics
GET /api/metrics/{serviceName}

# Record a request
POST /api/metrics/record
{
  "serviceName": "my-service",
  "latency": 150,
  "success": true
}

# Record a retry
POST /api/metrics/retry/{serviceName}

# Get latency statistics
GET /api/metrics/latency/{serviceName}

# Reset metrics
DELETE /api/metrics/{serviceName}
```

### Demo Endpoints

```bash
# Trigger a test request
POST /api/demo/trigger/{serviceName}

# Trigger all services
POST /api/demo/trigger-all

# Start load test
POST /api/demo/start-load?durationSeconds=10

# Get dashboard data
GET /api/demo/dashboard-data

# Reset demo data
POST /api/demo/reset-all
```

## 📈 Monitoring

### Prometheus Metrics

Metrics are exposed at `/actuator/prometheus`:

- `retry_budget_allocated` - Allocated retry budget per service
- `retry_budget_remaining` - Remaining retry budget per service
- `service_requests_total` - Total requests per service
- `service_errors_total` - Total errors per service
- `service_latency_seconds` - Request latency distribution

### Dashboard Features

- Real-time service health status
- Live metrics: requests, errors, latency percentiles
- Retry budget visualization with color-coded bars
- Request logs table with filtering
- WebSocket-powered live updates

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Manual Testing

1. **Test Healthy Service**:
   ```bash
   curl -X POST http://localhost:8080/api/demo/trigger/healthy-service
   ```

2. **Test Critical Service**:
   ```bash
   curl -X POST http://localhost:8080/api/demo/trigger/critical-service
   ```

3. **Check Budget**:
   ```bash
   curl http://localhost:8080/api/budget/critical-service
   ```

4. **View Metrics**:
   ```bash
   curl http://localhost:8080/api/metrics/critical-service
   ```

## 🔍 Integration Example

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final BudgetEnforcementAgent budgetAgent;
    private final MetricsAggregator metricsAggregator;

    public Response callExternalService() {
        String serviceName = "external-api";
        long startTime = System.currentTimeMillis();

        try {
            // Check if retry is allowed
            RetryDecision decision = budgetAgent.checkRetryAllowed(serviceName);

            if (decision == RetryDecision.DENY) {
                throw new RetryBudgetExceededException("Budget exceeded");
            }

            // Make the request
            Response response = restClient.get()
                .uri("https://external-api.com/endpoint")
                .retrieve()
                .body(Response.class);

            // Record success
            long duration = System.currentTimeMillis() - startTime;
            metricsAggregator.recordRequest(serviceName, duration, true);

            return response;

        } catch (Exception e) {
            // Record failure
            long duration = System.currentTimeMillis() - startTime;
            metricsAggregator.recordRequest(serviceName, duration, false);

            throw e;
        }
    }
}
```

## 📚 Documentation

- [Demo Guide](DEMO.md) - Comprehensive guide for using the demo
- [Architecture](docs/architecture.md) - Detailed architecture documentation (TODO)
- [API Reference](docs/api.md) - Full API documentation (TODO)

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

Based on research from:
- [Google SRE Book - Handling Overload](https://sre.google/sre-book/handling-overload/)
- [Marc Brooker - Retry Budgets](https://www.usenix.org/system/files/conference/srecon16/srecon16-brooker.pdf)
- [Netflix Hystrix Circuit Breaker](https://github.com/Netflix/Hystrix)

## 📧 Support

For issues and questions:
- Open an issue on GitHub
- Check the [Demo Guide](DEMO.md) for troubleshooting

---

**Built with ❤️ using Spring Boot 3.2.5, Redis, and WebSockets**
