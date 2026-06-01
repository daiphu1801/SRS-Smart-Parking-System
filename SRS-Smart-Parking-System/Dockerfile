# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Sao chép pom.xml trước để cache các thư viện dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Sao chép mã nguồn và tiến hành build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the production image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Thêm user non-root để tăng cường bảo mật
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Sao chép file jar đã build từ Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Mở port mặc định của Spring Boot
EXPOSE 8080

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
