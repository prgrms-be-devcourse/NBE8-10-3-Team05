#!/bin/bash
# scripts/setup_es_server.sh

# 1. 스왑(Swap) 설정 - t3.micro의 부족한 메모리 보완
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# 2. 패키지 설치
apt-get update -y && apt-get install -y docker.io docker-compose

# 3. Docker 시작 및 권한 설정
systemctl start docker
systemctl enable docker
usermod -aG docker ubuntu

# 4. Elasticsearch 필수 커널 설정 (메모리 맵핑 제한 해제)
sysctl -w vm.max_map_count=262144
echo 'vm.max_map_count=262144' >> /etc/sysctl.conf

# 5. 작업 디렉토리 생성
mkdir -p /home/ubuntu/app

# 6. docker-compose.yml 작성
cat <<EOT > /home/ubuntu/app/docker-compose.yml
version: '3.8'
services:
  elasticsearch:
    restart: always
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.3
    container_name: elasticsearch
    ports:
      - "9200:9200"
      - "9300:9300"
    environment:
      - discovery.type=single-node
      - network.host=0.0.0.0
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms256m -Xmx256m
    volumes:
      - es-data:/usr/share/elasticsearch/data
    ulimits:
      memlock:
        soft: -1
        hard: -1

  elasticsearch-exporter:
    restart: always
    image: prometheuscommunity/elasticsearch-exporter:latest
    container_name: elasticsearch-exporter
    ports:
      - "9114:9114"
    command:
      - "--es.uri=http://elasticsearch:9200"

volumes:
  es-data:
EOT

# 7. 실행
cd /home/ubuntu/app && docker-compose up -d
