# Latency-Aware Retry Budget - Demo Guide

This demo showcases a sophisticated retry budget allocation system that dynamically adjusts retry budgets based on service health, latency, and error rates.

## 🎯 What This Demo Shows

The demo demonstrates how the retry budget system:

1. **Monitors service health** in real-time based on latency and error rates
2. **Dynamically allocates retry budgets** - healthy services get more retries, unhealthy services get fewer
3. **Prevents cascading failures** by limiting retries to degraded/critical services
4. **Provides real-time visibility** into service health and retry decisions

## 🏗️ Architecture

### Demo Services

The demo includes 4 mock services with different characteristics:

1. **Healthy Service**
   - Latency: 50-150ms
   - Success Rate: 95%
   - Expected Health: HEALTHY
   - Expected Budget: Full (5 retries)

2. **Degraded Service**
   - Latency: 200-400ms
   - Success Rate: 80%
   - Expected Health: DEGRADED
   - Expected Budget: Reduced (~2-3 retries)

3. **Critical Service**
   - Latency: 500-1000ms
   - Success Rate: 50%
   - Expected Health: CRITICAL
   - Expected Budget: Minimal (~0-1 retries)

4. **Intermittent Service**
   - Alternates between healthy and unhealthy every 10 seconds
   - Demonstrates budget adaptation over time

### Components

- **DemoServiceController**: Simulates backend services with varying health
- **DemoClientService**: Makes requests with retry budget awareness
- **DemoController**: Orchestrates demo scenarios and provides dashboard data
- **WebSocketBroadcastService**: Pushes real-time updates to the dashboard
- **Frontend Dashboard**: Real-time visualization of service health and metrics

## 🚀 Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker & Docker Compose (for Redis)

### Step 1: Start Redis

```bash
docker-compose up -d
```

### Step 2: Build and Run the Application

```bash
# Build the application
mvn clean install -DskipTests

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Step 3: Open the Dashboard

Open your browser and navigate to:
```
http://localhost:8080
```

You should see the real-time dashboard with 4 service cards.

## 🎮 Using the Demo

### Dashboard Features

The dashboard shows:

1. **Service Cards** (4 services)
   - Health Status Badge (HEALTHY/DEGRADED/CRITICAL)
   - Key Metrics:
     - Total Requests
     - Success Rate
     - Error Rate
     - Total Retries
     - P95/P99 Latency
   - Retry Budget Bar (visual representation of remaining budget)

2. **Request Logs Table**
   - Real-time log of all requests
   - Shows: timestamp, service, status, duration, retries, result

3. **Control Buttons**
   - Test individual services
   - Test all services at once
   - Run load test (10 seconds of continuous requests)
   - Reset all data

### Try These Scenarios

#### Scenario 1: Basic Health Monitoring

1. Click "Test Healthy" several times
2. Observe: Service stays HEALTHY, budget remains high
3. Check logs: Most requests succeed on first try

#### Scenario 2: Degraded Service

1. Click "Test Degraded" several times
2. Observe: Service becomes DEGRADED, budget reduces
3. Check logs: More retries needed, some budget denials

#### Scenario 3: Critical Service Protection

1. Click "Test Critical" several times
2. Observe: Service becomes CRITICAL, budget drops to near-zero
3. Check logs: Many failures, retries denied to prevent cascading failures

#### Scenario 4: Mixed Load

1. Click "Test All Services"
2. Observe: Each service behaves according to its health
3. Notice: System prevents overwhelming unhealthy services

#### Scenario 5: Continuous Load

1. Click "Start Load Test (10s)"
2. Watch the dashboard update in real-time
3. Observe how budgets adapt over time
4. Notice the intermittent service alternating states

## 📊 Understanding the Metrics

### Health Status Determination

- **HEALTHY**: P95 latency < 200ms AND error rate < 5%
- **DEGRADED**: P95 latency 200-800ms OR error rate 5-10%
- **CRITICAL**: P99 latency > 800ms OR error rate > 10%

### Budget Calculation

Budget = BaseBudget × LatencyFactor × ErrorFactor

- **BaseBudget**: 5 (configurable)
- **LatencyFactor**: 0.0 - 1.0 (lower latency = higher factor)
- **ErrorFactor**: 0.0 - 1.0 (lower error rate = higher factor)

### Retry Decision Logic

```
IF service is CRITICAL → DENY (protect from cascading failures)
IF budget exhausted → DENY (prevent retry storm)
IF service is DEGRADED → DEFER (backoff and retry cautiously)
ELSE → ALLOW (healthy service, retry freely)
```

## 🔧 API Endpoints

### Demo Endpoints

```bash
# Trigger a single service
POST /api/demo/trigger/{serviceName}

# Trigger all services
POST /api/demo/trigger-all

# Start load test
POST /api/demo/start-load?durationSeconds=10

# Get dashboard data
GET /api/demo/dashboard-data

# Get logs for specific service
GET /api/demo/logs/{serviceName}

# Clear all logs
POST /api/demo/clear-logs

# Reset all demo data
POST /api/demo/reset-all
```

### Budget Management Endpoints

```bash
# Get budget for a service
GET /api/budget/{serviceName}

# Calculate budget for a service
POST /api/budget/calculate/{serviceName}

# Check if retry is allowed
GET /api/budget/check/{serviceName}

# Consume budget (record a retry)
POST /api/budget/consume/{serviceName}
```

### Metrics Endpoints

```bash
# Get metrics for a service
GET /api/metrics/{serviceName}

# Record a request
POST /api/metrics/record
Body: {
  "serviceName": "string",
  "latency": number,
  "success": boolean
}

# Record a retry
POST /api/metrics/retry/{serviceName}

# Get latency statistics
GET /api/metrics/latency/{serviceName}

# Reset metrics
DELETE /api/metrics/{serviceName}
```

## 🧪 Testing with cURL

```bash
# Test healthy service
curl -X POST http://localhost:8080/api/demo/trigger/healthy-service

# Test critical service
curl -X POST http://localhost:8080/api/demo/trigger/critical-service

# Get dashboard data
curl http://localhost:8080/api/demo/dashboard-data | jq

# Check retry budget
curl http://localhost:8080/api/budget/healthy-service | jq

# Get metrics
curl http://localhost:8080/api/metrics/healthy-service | jq
```

## 📈 Observability

### Prometheus Metrics

The application exposes Prometheus metrics at:
```
http://localhost:8080/actuator/prometheus
```

Key metrics:
- `retry_budget_allocated`
- `retry_budget_remaining`
- `service_requests_total`
- `service_errors_total`
- `service_latency_seconds`

### Real-Time Updates

The dashboard uses WebSocket (STOMP over SockJS) for real-time updates:
- Connection endpoint: `/ws`
- Dashboard updates: `/topic/dashboard` (broadcasts every 1 second)
- Events: `/topic/events`

## 🎯 Key Observations

As you use the demo, notice:

1. **Healthy services maintain full retry budget** - they rarely need retries
2. **Degraded services get reduced budget** - preventing wasted effort
3. **Critical services get minimal/no retries** - protecting the system
4. **Budgets adapt over time** - as service health changes
5. **The system prevents retry storms** - budget enforcement works
6. **Intermittent service shows budget adaptation** - watch it switch states

## 🔄 Reset and Restart

To reset the demo to a clean state:

1. Click "Reset All" button in the dashboard, OR
2. Restart the application
3. Redis data has TTL of 300 seconds (auto-expires)

## 🐛 Troubleshooting

### Dashboard Not Loading

- Check if the application is running: `curl http://localhost:8080/actuator/health`
- Check browser console for errors
- Verify WebSocket connection status (indicator in top-left)

### No Metrics Showing

- Trigger some requests using the test buttons
- Wait a few seconds for metrics to aggregate
- Check if Redis is running: `docker ps`

### WebSocket Connection Failed

- Check if port 8080 is accessible
- Verify no firewall blocking WebSocket connections
- Check browser console for detailed error messages

## 📚 Next Steps

To integrate this into a real system:

1. Replace mock services with actual service endpoints
2. Configure thresholds in `application.yml` for your SLAs
3. Set up Prometheus/Grafana for monitoring
4. Implement circuit breaker patterns alongside retry budgets
5. Add distributed tracing (e.g., OpenTelemetry)

## 🎓 Learn More

- [Spring Retry Documentation](https://docs.spring.io/spring-retry/docs/current/reference/html/)
- [Circuit Breaker Pattern](https://martinfowler.com/bliki/CircuitBreaker.html)
- [Google SRE Book - Handling Overload](https://sre.google/sre-book/handling-overload/)
- [Retry Budget Paper](https://www.usenix.org/system/files/conference/srecon16/srecon16-brooker.pdf)

---

**Enjoy exploring the Latency-Aware Retry Budget system!** 🚀
