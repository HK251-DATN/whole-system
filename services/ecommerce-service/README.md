## 1. Define core: domain & logic & entity
```
com.example.ecommerce
├── EcommerceApplication.java
│
├── domain // Core business (pure Java)
│ ├── entity // Entity, Value Object
│ │ ├── Order.java
│ │ └── OrderItem.java
│ ├── usecase // Interface use-case
│ │ └── CreateOrderUseCase.java
│ ├── service // Domain service
│ │ └── PricingService.java
│ └── exception
│ └── BusinessException.java
│
├── persistence // get data from database
│ ├── repository
│ │ └── SaleProductRepository.java
│ ├── dto
│   ├── SaleProductDTO.java
│   └── SaleEventDTO.java
|
├── infrastructure // Framework & DB
│ ├── config
│ │ └── JpaConfig.java
│ └── messaging
│ └── KafkaProducer.java
│
├── presentation // Delivery layer
│ ├── rest
│ │ └── OrderController.java
│ ├── request
│ │ └── CreateOrderRequest.java
│ └── response
| |__ mapping // mapping request <-> response || dto <-> response || request <-> entity
│
└── common
  |__ utils
```

## 4. application.yml
```yaml
# <!-- create ecommerce/main/resource/application.yaml -->

spring:
  application:
    name: ecommerce

  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:update}
    properties:
      hibernate:
        format_sql: true
    show-sql: true

server:
  port: ${SERVER_PORT:8080}


```

## 5. create database
```
potgres: db_name = datnecommerce
```

## 6. import api json to postman
```
ecom_dacn.json
```

## 7. container run
```sh
# requirement docker engine
cd ecommerce
docker-compose up --build
```
