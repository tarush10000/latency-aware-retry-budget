#!/bin/bash

echo "================================================"
echo "Latency-Aware Retry Budget - Demo Startup"
echo "================================================"
echo ""

# Check if Redis is running
echo "Checking Redis..."
if ! docker ps | grep -q retry-budget-redis; then
    echo "Starting Redis with Docker Compose..."
    docker-compose up -d
    echo "Waiting for Redis to be ready..."
    sleep 5
else
    echo "Redis is already running"
fi

echo ""
echo "Building the application..."
mvn clean install -DskipTests

if [ $? -eq 0 ]; then
    echo ""
    echo "================================================"
    echo "Starting the application..."
    echo "================================================"
    echo ""
    echo "Dashboard will be available at: http://localhost:8080"
    echo "Press Ctrl+C to stop"
    echo ""
    mvn spring-boot:run
else
    echo ""
    echo "Build failed. Please check the error messages above."
    exit 1
fi
