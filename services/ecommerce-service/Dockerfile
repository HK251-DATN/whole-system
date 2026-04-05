FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21
WORKDIR /app
# copy built jar (support wildcard for artifact name)
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 9301
ENTRYPOINT ["java","-jar","/app/app.jar"]
