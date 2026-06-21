# E-Commerce Microservices Platform - Architecture Diagrams

## 1. High-Level System Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        UI1[Ecommerce UI<br/>React + Vite<br/>Port 3000<br/>Customers]
        UI2[Back-Office UI<br/>React + Vite + MUI<br/>Port 5173<br/>Admins]
        UI3[Provider UI<br/>React + Vite + Ant Design<br/>Port 5273<br/>Farmers]
    end
    
    subgraph "Microservices Layer"
        IS[Identity Service<br/>Spring Boot 4.0.1<br/>Java 25<br/>Port 9000<br/>OAuth2]
        BOS[Back-Office Service<br/>Spring Boot 4.0.2<br/>Java 25<br/>Port 9100]
        PSS[Product Storage Service<br/>Spring Boot 4.0.3<br/>Java 25<br/>Port 9200]
        ES[Ecommerce Service<br/>Spring Boot 3.5.6<br/>Java 21<br/>Port 9300<br/>Clean Architecture]
    end
    
    subgraph "Event Streaming Layer"
        KAFKA[Apache Kafka 4.2.0<br/>Event Bus]
        KUI[Kafka UI<br/>Port 9280]
    end
    
    subgraph "Data Layer"
        DB1[(identity_db<br/>PostgreSQL 17)]
        DB2[(back_office_db<br/>PostgreSQL 17)]
        DB3[(product_storage_db<br/>PostgreSQL 17)]
        DB4[(ecommerce_db_2<br/>PostgreSQL 17)]
        PGA[pgAdmin<br/>Port 5480]
    end
    
    subgraph "External Services"
        R2[Cloudflare R2<br/>Object Storage<br/>User Avatars<br/>Product Images]
        GOONG[Goong Maps API<br/>Vietnamese Maps<br/>Address Validation]
    end
    
    UI1 -.HTTP.-> IS
    UI1 -.HTTP.-> ES
    UI2 -.HTTP.-> IS
    UI2 -.HTTP.-> BOS
    UI2 -.HTTP.-> PSS
    UI3 -.HTTP.-> IS
    UI3 -.HTTP.-> ES
    
    IS <-.REST.-> BOS
    IS <-.REST.-> ES
    BOS -.REST.-> PSS
    
    IS -->|user-events| KAFKA
    BOS -->|category-events<br/>subsubcategory-events<br/>product-general-events| KAFKA
    KAFKA -->|product-general-events<br/>subsubcategory-events| PSS
    PSS -->|batch-detail-events| KAFKA
    KAFKA -->|batch-detail-events| ES
    ES -->|order-item-events| KAFKA
    KAFKA -->|order-item-events| PSS
    
    IS --> DB1
    BOS --> DB2
    PSS --> DB3
    ES --> DB4
    
    IS -.Upload/Download.-> R2
    BOS -.Upload/Download.-> R2
    ES -.Geocoding.-> GOONG
    
    PGA -.Manage.-> DB1
    PGA -.Manage.-> DB2
    PGA -.Manage.-> DB3
    PGA -.Manage.-> DB4
    KUI -.Monitor.-> KAFKA
    
    style IS fill:#e1f5ff
    style BOS fill:#e1f5ff
    style PSS fill:#e1f5ff
    style ES fill:#fff4e1
    style KAFKA fill:#ffe1e1
    style UI1 fill:#e8f5e9
    style UI2 fill:#e8f5e9
    style UI3 fill:#e8f5e9
```

## 2. Event-Driven Communication Flow

```mermaid
sequenceDiagram
    participant BO as Back-Office Service
    participant K as Apache Kafka
    participant PS as Product Storage Service
    participant EC as Ecommerce Service
    
    Note over BO,EC: Product Catalog Flow
    
    BO->>K: Publish category-events
    BO->>K: Publish subsubcategory-events
    BO->>K: Publish product-general-events
    
    K->>PS: Consume product-general-events
    K->>PS: Consume subsubcategory-events
    
    Note over PS: Batch Processing Logic<br/>Converts bulk batches to sellable units<br/>10kg batch + 500g package = 20 products
    
    PS->>K: Publish batch-detail-events
    
    K->>EC: Consume batch-detail-events
    
    Note over EC: Create sellable products<br/>with pricing and inventory
    
    Note over BO,EC: Order Processing Flow
    
    EC->>K: Publish order-item-events
    K->>PS: Consume order-item-events
    
    Note over PS: Update inventory<br/>based on orders
```

## 3. Kafka Topics and Event Flow

```mermaid
graph LR
    subgraph "Producers"
        IS[Identity Service]
        BOS[Back-Office Service]
        PSS[Product Storage Service]
        ES[Ecommerce Service]
    end
    
    subgraph "Kafka Topics (3 partitions each)"
        T1[user-events]
        T2[category-events]
        T3[subsubcategory-events]
        T4[product-general-events]
        T5[batch-detail-events]
        T6[order-events]
        T7[order-item-events]
        T8[payment-events]
    end
    
    subgraph "Consumers"
        PSS2[Product Storage Service]
        ES2[Ecommerce Service]
    end
    
    IS -->|Publish| T1
    BOS -->|Publish| T2
    BOS -->|Publish| T3
    BOS -->|Publish| T4
    
    T3 -->|Subscribe| PSS2
    T4 -->|Subscribe| PSS2
    
    PSS -->|Publish| T5
    T5 -->|Subscribe| ES2
    
    ES -->|Publish| T6
    ES -->|Publish| T7
    ES -->|Publish| T8
    
    T7 -->|Subscribe| PSS2
    
    style T1 fill:#ffe1e1
    style T2 fill:#ffe1e1
    style T3 fill:#ffe1e1
    style T4 fill:#ffe1e1
    style T5 fill:#ffe1e1
    style T6 fill:#ffe1e1
    style T7 fill:#ffe1e1
    style T8 fill:#ffe1e1
```

## 4. Database Architecture (Database-per-Service Pattern)

```mermaid
graph TB
    subgraph "PostgreSQL 17 Instance :5432"
        DB1[(identity_db)]
        DB2[(back_office_db)]
        DB3[(product_storage_db)]
        DB4[(ecommerce_db_2)]
    end
    
    subgraph "Services"
        IS[Identity Service<br/>:9000]
        BOS[Back-Office Service<br/>:9100]
        PSS[Product Storage Service<br/>:9200]
        ES[Ecommerce Service<br/>:9300]
    end
    
    IS -.JPA/Hibernate.-> DB1
    BOS -.JPA/Hibernate.-> DB2
    PSS -.JPA/Hibernate.-> DB3
    ES -.JPA/Hibernate.-> DB4
    
    PGA[pgAdmin Web UI<br/>:5480]
    PGA -.Manage All.-> DB1
    PGA -.Manage All.-> DB2
    PGA -.Manage All.-> DB3
    PGA -.Manage All.-> DB4
    
    subgraph "DB1 Schema"
        U1[users<br/>sessions<br/>credentials<br/>roles]
    end
    
    subgraph "DB2 Schema"
        U2[categories<br/>subcategories<br/>subsubcategories<br/>product_general]
    end
    
    subgraph "DB3 Schema"
        U3[warehouses<br/>storage_tools<br/>product_batches<br/>product_details<br/>inventory]
    end
    
    subgraph "DB4 Schema"
        U4[buyers<br/>orders<br/>order_items<br/>carts<br/>sale_events<br/>reviews]
    end
    
    DB1 --- U1
    DB2 --- U2
    DB3 --- U3
    DB4 --- U4
    
    style DB1 fill:#e1f5ff
    style DB2 fill:#e1f5ff
    style DB3 fill:#e1f5ff
    style DB4 fill:#e1f5ff
```

## 5. Service Internal Architecture Comparison

### Identity/Back-Office/Product Storage Services (Layered Architecture)

```mermaid
graph TB
    subgraph "Layered Architecture (Identity, Back-Office, Product Storage)"
        C1[Controller Layer<br/>REST endpoints<br/>@RestController]
        S1[Service Layer<br/>Business logic<br/>@Service]
        R1[Repository Layer<br/>Data access<br/>@Repository]
        D1[DAO Layer<br/>JPA entities<br/>@Entity]
        
        C1 --> S1
        S1 --> R1
        R1 --> D1
        
        M1[Messaging Layer<br/>@KafkaListener<br/>KafkaTemplate]
        S1 <--> M1
        
        CF1[Config Layer<br/>Security<br/>CORS<br/>Kafka]
        
        E1[Exception Layer<br/>Global handlers<br/>Custom exceptions]
        C1 --> E1
    end
    
    style C1 fill:#e8f5e9
    style S1 fill:#fff4e1
    style R1 fill:#e1f5ff
    style D1 fill:#f3e5f5
    style M1 fill:#ffe1e1
```

### Ecommerce Service (Clean Architecture)

```mermaid
graph TB
    subgraph "Clean Architecture (Ecommerce Service)"
        subgraph "Presentation Layer"
            REST[REST Controllers<br/>Request/Response DTOs<br/>Mappers]
        end
        
        subgraph "Domain Layer (Core)"
            E[Entities<br/>Pure business objects]
            UC[Use Cases<br/>Business rules<br/>Interfaces]
            DS[Domain Services<br/>Business logic]
        end
        
        subgraph "Infrastructure Layer"
            K[Messaging<br/>Kafka consumers/producers]
            SEC[Security<br/>JWT filters]
            CFG[Configuration<br/>Spring configs]
        end
        
        subgraph "Persistence Layer"
            REPO[Repositories<br/>JPA implementations]
            DTO[Database DTOs<br/>@Entity classes]
        end
        
        REST --> UC
        UC --> DS
        UC --> E
        
        REPO -.implements.-> UC
        REPO --> DTO
        
        K -.implements.-> UC
        SEC --> REST
        
        REST -.depends on.-> UC
        REPO -.depends on.-> E
        K -.depends on.-> E
    end
    
    style E fill:#f3e5f5
    style UC fill:#fff4e1
    style DS fill:#fff4e1
    style REST fill:#e8f5e9
    style REPO fill:#e1f5ff
    style K fill:#ffe1e1
```

## 6. Frontend-Backend Integration

```mermaid
graph TB
    subgraph "Frontend Applications"
        UI1[Ecommerce UI<br/>Customer Portal<br/>React 19 + Vite<br/>:3000]
        UI2[Back-Office UI<br/>Admin Panel<br/>React 19 + Vite + TS<br/>Material-UI + Ant Design<br/>Redux Toolkit + React Query<br/>:5173]
        UI3[Provider UI<br/>Farmer Portal<br/>React 19 + Vite + TS<br/>Ant Design + TanStack Query<br/>Zustand<br/>:5273<br/>Vietnamese Language]
    end
    
    subgraph "API Layer"
        IS[Identity Service<br/>:9000/api<br/>OAuth2]
        BOS[Back-Office Service<br/>:9100/api]
        PSS[Product Storage Service<br/>:9200/api]
        ES[Ecommerce Service<br/>:9300/api<br/>JWT]
    end
    
    UI1 -->|Authentication| IS
    UI1 -->|Browse/Order Products| ES
    
    UI2 -->|Authentication| IS
    UI2 -->|Manage Products| BOS
    UI2 -->|Manage Inventory| PSS
    UI2 -->|View Orders| ES
    
    UI3 -->|Authentication| IS
    UI3 -->|View Transactions| ES
    UI3 -->|Track Demand| ES
    
    IS -.CORS: localhost:3000, 5173, 5273.-> UI1
    IS -.CORS: localhost:3000, 5173, 5273.-> UI2
    IS -.CORS: localhost:3000, 5173, 5273.-> UI3
    
    style UI1 fill:#e8f5e9
    style UI2 fill:#e8f5e9
    style UI3 fill:#e8f5e9
    style IS fill:#e1f5ff
    style BOS fill:#e1f5ff
    style PSS fill:#e1f5ff
    style ES fill:#fff4e1
```

## 7. Docker Compose Deployment Architecture

```mermaid
graph TB
    subgraph "Docker Network: ecommerce-network"
        subgraph "Frontend Layer (Local Dev)"
            F1[ecommerce-ui<br/>npm run dev<br/>:3000]
            F2[back-office-ui<br/>npm run dev<br/>:5173]
            F3[provider-ui<br/>npm run dev<br/>:5273]
        end
        
        subgraph "Service Containers"
            S1[identity-service<br/>:9000<br/>Image: openjdk:25]
            S2[back-office-service<br/>:9100<br/>Image: openjdk:25]
            S3[product-storage-service<br/>:9200<br/>Image: openjdk:25]
            S4[ecommerce-service<br/>:9300<br/>Image: openjdk:21]
        end
        
        subgraph "Infrastructure Containers"
            DB[postgres:17<br/>:5432<br/>4 databases<br/>Volume: postgres_data]
            PGA[pgadmin<br/>:5480<br/>Volume: pgadmin_data]
            KF[kafka:4.2.0<br/>:9092<br/>Volume: kafka_data]
            KUI[kafka-ui<br/>:9280]
        end
        
        F1 -.HTTP.-> S1
        F1 -.HTTP.-> S4
        F2 -.HTTP.-> S1
        F2 -.HTTP.-> S2
        F2 -.HTTP.-> S3
        F3 -.HTTP.-> S1
        F3 -.HTTP.-> S4
        
        S1 --> DB
        S2 --> DB
        S3 --> DB
        S4 --> DB
        
        S1 --> KF
        S2 --> KF
        S3 --> KF
        S4 --> KF
        
        PGA -.Manage.-> DB
        KUI -.Monitor.-> KF
    end
    
    subgraph "External Services"
        R2[Cloudflare R2<br/>Object Storage]
        GM[Goong Maps API]
    end
    
    S1 -.Upload.-> R2
    S2 -.Upload.-> R2
    S4 -.Geocode.-> GM
    
    ENV[.env file<br/>R2_ACCOUNT_ID<br/>R2_ACCESS_KEY<br/>R2_SECRET_KEY<br/>GOONG_API_KEY<br/>DB credentials<br/>KAFKA config]
    
    ENV -.Configure.-> S1
    ENV -.Configure.-> S2
    ENV -.Configure.-> S3
    ENV -.Configure.-> S4
    
    style S1 fill:#e1f5ff
    style S2 fill:#e1f5ff
    style S3 fill:#e1f5ff
    style S4 fill:#fff4e1
    style DB fill:#f3e5f5
    style KF fill:#ffe1e1
    style F1 fill:#e8f5e9
    style F2 fill:#e8f5e9
    style F3 fill:#e8f5e9
```

## 8. Service Dependency and Startup Order

```mermaid
graph TD
    START[Start System]
    
    START --> INFRA[Infrastructure Layer]
    
    subgraph "Infrastructure (Start First)"
        DB[PostgreSQL<br/>4 databases]
        KAFKA[Kafka Broker]
    end
    
    INFRA --> DB
    INFRA --> KAFKA
    
    DB --> TIER1[Independent Services]
    KAFKA --> TIER1
    
    subgraph "Tier 1: No Dependencies"
        IS[Identity Service<br/>:9000]
        BOS[Back-Office Service<br/>:9100]
    end
    
    TIER1 --> IS
    TIER1 --> BOS
    
    BOS -->|product-general-events<br/>subsubcategory-events| TIER2[Dependent Services]
    
    subgraph "Tier 2: Consumes Back-Office Events"
        PSS[Product Storage Service<br/>:9200]
    end
    
    TIER2 --> PSS
    
    PSS -->|batch-detail-events| TIER3[Final Services]
    
    subgraph "Tier 3: Consumes Storage Events"
        ES[Ecommerce Service<br/>:9300]
    end
    
    TIER3 --> ES
    
    ES --> FRONTEND[Frontend Layer]
    IS --> FRONTEND
    BOS --> FRONTEND
    PSS --> FRONTEND
    
    subgraph "Frontend (Start Last or Run Locally)"
        UI1[Ecommerce UI :3000]
        UI2[Back-Office UI :5173]
        UI3[Provider UI :5273]
    end
    
    FRONTEND --> UI1
    FRONTEND --> UI2
    FRONTEND --> UI3
    
    style DB fill:#f3e5f5
    style KAFKA fill:#ffe1e1
    style IS fill:#e1f5ff
    style BOS fill:#e1f5ff
    style PSS fill:#e1f5ff
    style ES fill:#fff4e1
    style UI1 fill:#e8f5e9
    style UI2 fill:#e8f5e9
    style UI3 fill:#e8f5e9
```

## 9. Category Hierarchy and Data Flow

```mermaid
graph TB
    subgraph "Back-Office Service"
        CAT[Category<br/>e.g., Food]
        SUBCAT[Subcategory<br/>e.g., Vegetables]
        SUBSUBCAT[SubSubcategory<br/>e.g., Leafy Greens]
        PG[Product General<br/>e.g., Fresh Spinach<br/>Unit: MASS]
        
        CAT --> SUBCAT
        SUBCAT --> SUBSUBCAT
        SUBSUBCAT --> PG
    end
    
    PG -->|product-general-events| KAFKA1[Kafka]
    SUBSUBCAT -->|subsubcategory-events| KAFKA1
    
    subgraph "Product Storage Service"
        KAFKA1 --> PG2[Product General<br/>Replica]
        KAFKA1 --> SSC2[SubSubCategory<br/>Replica]
        
        WH[Warehouse<br/>Location: Hanoi]
        ST[Storage Tool<br/>Fridge A1]
        PB[Product Batch<br/>10kg bulk spinach]
        
        WH --> ST
        ST --> PB
        PG2 --> PB
        
        PB -->|Batch Processing<br/>10kg ÷ 500g = 20 units| PD[Product Details<br/>20x 500g packages<br/>Price: 25,000 VND each<br/>Expiration: 2026-05-15]
    end
    
    PD -->|batch-detail-events| KAFKA2[Kafka]
    
    subgraph "Ecommerce Service"
        KAFKA2 --> BD[Batch Detail<br/>Sellable Product]
        BD --> SE[Sale Event<br/>10% discount]
        BD --> CART[Shopping Cart]
        BD --> REV[Product Review]
        
        CART --> ORD[Order]
        ORD --> ORDI[Order Items]
    end
    
    ORDI -->|order-item-events| KAFKA3[Kafka]
    KAFKA3 --> INV[Update Inventory<br/>in Product Storage]
    
    style CAT fill:#e8f5e9
    style SUBCAT fill:#e8f5e9
    style SUBSUBCAT fill:#e8f5e9
    style PG fill:#e1f5ff
    style PB fill:#fff4e1
    style PD fill:#fff4e1
    style BD fill:#f3e5f5
    style KAFKA1 fill:#ffe1e1
    style KAFKA2 fill:#ffe1e1
    style KAFKA3 fill:#ffe1e1
```

## 10. Authentication Flow

### OAuth2 Flow (Identity Service, Back-Office Service, Product Storage Service)

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Frontend App
    participant IS as Identity Service<br/>(OAuth2)
    participant BOS as Back-Office Service
    
    U->>UI: Login (username/password)
    UI->>IS: POST /oauth/token
    IS->>IS: Validate credentials
    IS-->>UI: Access Token + Refresh Token
    UI->>UI: Store tokens (localStorage)
    
    U->>UI: Request protected resource
    UI->>BOS: GET /api/products<br/>Authorization: Bearer {token}
    BOS->>IS: Validate token (REST call)
    IS-->>BOS: Token valid + user info
    BOS-->>UI: Protected resource
    
    Note over UI,BOS: Token expires
    
    UI->>IS: POST /oauth/token<br/>(refresh_token)
    IS-->>UI: New access token
```

### JWT Flow (Ecommerce Service)

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Ecommerce UI
    participant ES as Ecommerce Service<br/>(Custom JWT)
    
    U->>UI: Login (username/password)
    UI->>ES: POST /api/auth/login
    ES->>ES: Validate credentials<br/>Generate JWT
    ES-->>UI: JWT Token
    UI->>UI: Store JWT (localStorage)
    
    U->>UI: Add item to cart
    UI->>ES: POST /api/cart<br/>Authorization: Bearer {JWT}
    ES->>ES: Validate JWT<br/>(Custom Filter)
    ES->>ES: Extract buyer ID from JWT
    ES-->>UI: Cart updated
    
    U->>UI: Place order
    UI->>ES: POST /api/orders<br/>Authorization: Bearer {JWT}
    ES->>ES: Validate JWT
    ES->>ES: Process order
    ES-->>UI: Order confirmation
```

## 11. Recommended Production Architecture (Future State)

```mermaid
graph TB
    subgraph "Client Tier"
        CDN[CloudFlare CDN<br/>Static Assets]
        UI[Frontend Apps<br/>S3/Nginx]
    end
    
    subgraph "API Gateway Tier"
        LB[Load Balancer<br/>AWS ALB/NLB]
        GW[API Gateway<br/>Spring Cloud Gateway<br/>Rate Limiting<br/>Circuit Breaker]
    end
    
    subgraph "Kubernetes Cluster"
        subgraph "Service Mesh (Istio/Linkerd)"
            IS[Identity Service<br/>3 replicas<br/>Auto-scaling]
            BOS[Back-Office Service<br/>3 replicas<br/>Auto-scaling]
            PSS[Product Storage<br/>5 replicas<br/>Auto-scaling]
            ES[Ecommerce Service<br/>5 replicas<br/>Auto-scaling]
        end
    end
    
    subgraph "Data Tier"
        RDS[Amazon RDS<br/>PostgreSQL Multi-AZ<br/>Read Replicas]
        CACHE[Redis Cluster<br/>ElastiCache<br/>Session Cache]
    end
    
    subgraph "Event Streaming"
        MSK[Amazon MSK<br/>Managed Kafka<br/>3 brokers<br/>Multi-AZ]
    end
    
    subgraph "Storage"
        S3[S3/R2<br/>Object Storage]
    end
    
    subgraph "Observability"
        PROM[Prometheus<br/>Metrics Collection]
        GRAF[Grafana<br/>Dashboards]
        ELK[ELK Stack<br/>Centralized Logging]
        TRACE[Jaeger/Zipkin<br/>Distributed Tracing]
    end
    
    CDN --> UI
    UI --> LB
    LB --> GW
    GW --> IS
    GW --> BOS
    GW --> PSS
    GW --> ES
    
    IS --> RDS
    BOS --> RDS
    PSS --> RDS
    ES --> RDS
    
    IS --> CACHE
    ES --> CACHE
    
    IS --> MSK
    BOS --> MSK
    PSS --> MSK
    ES --> MSK
    
    BOS --> S3
    IS --> S3
    
    IS -.metrics.-> PROM
    BOS -.metrics.-> PROM
    PSS -.metrics.-> PROM
    ES -.metrics.-> PROM
    
    PROM --> GRAF
    
    IS -.logs.-> ELK
    BOS -.logs.-> ELK
    PSS -.logs.-> ELK
    ES -.logs.-> ELK
    
    IS -.traces.-> TRACE
    BOS -.traces.-> TRACE
    PSS -.traces.-> TRACE
    ES -.traces.-> TRACE
    
    style IS fill:#e1f5ff
    style BOS fill:#e1f5ff
    style PSS fill:#e1f5ff
    style ES fill:#fff4e1
    style MSK fill:#ffe1e1
    style RDS fill:#f3e5f5
    style CACHE fill:#fff3e0
```

## 12. Technology Stack Overview

```mermaid
graph LR
    subgraph "Frontend Stack"
        FE[React 19<br/>Vite 8<br/>TypeScript/JavaScript]
        STATE[Redux Toolkit<br/>Zustand<br/>TanStack Query]
        UI_LIB[Material-UI 5<br/>Ant Design]
        HTTP[Axios]
        
        FE --> STATE
        FE --> UI_LIB
        FE --> HTTP
    end
    
    subgraph "Backend Stack"
        BE1[Spring Boot 4.0.x<br/>Java 25]
        BE2[Spring Boot 3.5.6<br/>Java 21]
        SEC[Spring Security<br/>OAuth2<br/>JWT]
        ORM[JPA/Hibernate]
        BUILD[Maven Wrapper]
        
        BE1 --> SEC
        BE1 --> ORM
        BE2 --> SEC
        BE2 --> ORM
        BE1 --> BUILD
        BE2 --> BUILD
    end
    
    subgraph "Infrastructure"
        DB[PostgreSQL 17]
        MSG[Apache Kafka 4.2.0]
        CONT[Docker<br/>Docker Compose 3.9]
        
        ORM --> DB
        SEC --> DB
        BE1 --> MSG
        BE2 --> MSG
    end
    
    subgraph "External"
        R2[Cloudflare R2<br/>S3-compatible]
        MAPS[Goong Maps API]
    end
    
    HTTP --> BE1
    HTTP --> BE2
    
    BE1 --> R2
    BE2 --> R2
    BE2 --> MAPS
    
    CONT -.Orchestrates.-> BE1
    CONT -.Orchestrates.-> BE2
    CONT -.Orchestrates.-> DB
    CONT -.Orchestrates.-> MSG
    
    style FE fill:#e8f5e9
    style BE1 fill:#e1f5ff
    style BE2 fill:#fff4e1
    style DB fill:#f3e5f5
    style MSG fill:#ffe1e1
```

---

## Diagram Legend

- **Blue boxes**: Spring Boot 4.x services (Java 25)
- **Yellow boxes**: Spring Boot 3.x service (Java 21) - Clean Architecture
- **Green boxes**: Frontend applications
- **Red boxes**: Event streaming (Kafka)
- **Purple boxes**: Databases
- **Solid arrows**: Direct calls/dependencies
- **Dashed arrows**: Asynchronous events or monitoring connections
