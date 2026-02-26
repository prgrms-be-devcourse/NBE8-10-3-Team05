FROM node:20-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

FROM gradle:8-jdk21 AS backend-builder
WORKDIR /app/backend
COPY backend/back/ ./
RUN chmod +x ./gradlew
RUN ./gradlew bootJar

FROM eclipse-temurin:21-jdk-jammy
RUN apt-get update && apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_20.x | bash - && \
    apt-get install -y nodejs
WORKDIR /app
COPY --from=frontend-builder /app/frontend /app/frontend
COPY --from=backend-builder /app/backend/build/libs/*.jar app.jar
EXPOSE 3000 8080
ENTRYPOINT ["sh", "-c", "cd /app/frontend && npm start & java -jar /app/app.jar"]
