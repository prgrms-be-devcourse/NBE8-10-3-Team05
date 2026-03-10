#!/bin/bash
# scripts/setup_monitor_server.sh

# 1. 패키지 설치 및 Docker 설정
sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker && sudo systemctl enable docker && sudo usermod -aG docker ubuntu

# 2. 작업 디렉토리 생성
mkdir -p /home/ubuntu/app

# 3. Prometheus 설정 파일 작성 (각 서버의 Exporter 연결)
cat <<EOT | sudo tee /home/ubuntu/app/prometheus.yml > /dev/null
global:
  scrape_interval: 15s
scrape_configs:
  - job_name: 'mysql'
    static_configs:
      - targets: ['${db_private_ip}:9104']
  - job_name: 'redis'
    static_configs:
      - targets: ['${redis_private_ip}:9121']
  - job_name: 'elasticsearch'
    static_configs:
      - targets: ['${es_private_ip}:9114']
  - job_name: 'spring-boot-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets:
        - '${was_1_private_ip}:8080'
        - '${was_2_private_ip}:8080'
    relabel_configs:
      - source_labels: [job]
        target_label: application
EOT

# 4. Docker Compose 파일 작성 (Grafana 서브패스 설정 포함)
cat <<EOT | sudo tee /home/ubuntu/app/docker-compose.yml > /dev/null
version: '3.8'
services:
  prometheus:
    restart: always
    image: prom/prometheus:latest
    ports: [ "9090:9090" ]
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
  grafana:
    restart: always
    image: grafana/grafana:latest
    ports: [ "3001:3000" ]
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_SERVER_ROOT_URL=https://${dns_name}/grafana/
      - GF_SERVER_SERVE_FROM_SUB_PATH=true
EOT

# 5. 실행
cd /home/ubuntu/app && sudo docker-compose up -d
