# Kafka Share

This repository hosts the Kafka configuration for inter-service communication in the capstone project.

## Overview

Provides a Docker Compose setup that initializes a Kafka broker and automatically creates required topics for event streaming across microservices.

## Services

### kafka

- **Image**: bitnami/kafka:latest
- **Port**: 9092 (PLAINTEXT)
- **Configuration**: Single-node KRaft cluster

### kafka-init

- Automatically creates Kafka topics on startup
- **Topics created**:
  - `user-events` (3 partitions)
  - `order-events` (3 partitions)

## Getting Started

```bash
docker-compose up -d
```

The Kafka broker will be available at `kafka:9092` for services within the Docker network.

## Topics

| Topic         | Partitions  | Replication Factor  |
|---------------|-------------|---------------------|
| user-events   | 3           | 1                   |
| order-events  | 3           | 1                   |
