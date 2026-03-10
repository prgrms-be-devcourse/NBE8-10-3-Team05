#!/bin/bash
# scripts/setup_redis_server.sh

# 1. Docker 및 Docker-compose 설치
sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker && sudo systemctl enable docker && sudo usermod -aG docker ubuntu

# 2. 앱 디렉토리 생성 및 docker-compose.yml 작성
mkdir -p /home/ubuntu/app
cat <<EOT > /home/ubuntu/app/docker-compose.yml
version: '3.8'
services:
  redis:
    restart: always
    image: redis:latest
    ports: [ "6379:6379" ]
  redis-exporter:
    restart: always
    image: oliver006/redis_exporter:latest
    ports: [ "9121:9121" ]
    environment:
      - REDIS_ADDR=redis:6379
EOT

# 3. 컨테이너 실행
cd /home/ubuntu/app && docker-compose up -d
