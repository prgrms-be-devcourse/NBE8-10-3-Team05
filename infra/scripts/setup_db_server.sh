#!/bin/bash
# scripts/setup_db_server.sh

# 1. Docker 및 Docker-compose 설치
sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker && sudo systemctl enable docker && sudo usermod -aG docker ubuntu

# 2. 앱 디렉토리 생성 및 docker-compose.yml 작성
mkdir -p /home/ubuntu/app
cat <<EOT > /home/ubuntu/app/docker-compose.yml
version: '3.8'
services:
  mysql:
    restart: always
    image: mysql:latest
    ports: [ "3306:3306" ]
    environment:
      - MYSQL_ROOT_PASSWORD=${db_password}
      - MYSQL_DATABASE=my_db
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${db_password}"]
      interval: 15s
      timeout: 10s
      retries: 5
      start_period: 40s
    deploy:
      resources:
        limits:
          memory: 800M
  mysql-exporter:
    restart: always
    image: prom/mysqld-exporter:latest
    ports: [ "9104:9104" ]
    command:
      - "--mysqld.address=mysql:3306"
      - "--mysqld.username=root:1234"
    environment:
      - DATA_SOURCE_NAME=root:${db_password}@(mysql:3306)/
    depends_on:
      mysql:
        condition: service_healthy
EOT

# 3. 컨테이너 실행
cd /home/ubuntu/app && docker-compose up -d
