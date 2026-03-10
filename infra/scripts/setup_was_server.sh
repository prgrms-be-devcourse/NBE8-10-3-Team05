#!/bin/bash
# scripts/setup_was_server.sh

# 1. 패키지 설치 및 Docker 설정
sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker && sudo systemctl enable docker && sudo usermod -aG docker ubuntu

# 2. GHCR(GitHub Container Registry) 로그인
echo "${github_token}" | docker login ghcr.io -u ${github_username} --password-stdin

# 3. Swap 설정 (t3.micro 메모리 부족 방지)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 4. 앱 디렉토리 생성 및 docker-compose.yml 작성
mkdir -p /home/ubuntu/app
cat <<EOT > /home/ubuntu/app/docker-compose.yml
version: '3.8'
services:
  app:
    restart: always
    image: ${docker_image_name}
    ports: [ "8080:8080" , "3000:3000" ]
    environment:
      - JAVA_OPTS=-Xms256m -Xmx512m
      - NODE_OPTIONS=--max-old-space-size=400

      # 1. Database
      - SPRING_DATASOURCE_URL=jdbc:mysql://${db_private_ip}:3306/my_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&rewriteBatchedStatements=true
      - SPRING_DATASOURCE_USERNAME=${db_username}
      - SPRING_DATASOURCE_PASSWORD=${db_password}
      - SPRING_DATASOURCE_DRIVER_CLASS_NAME=${db_driver_class_name}

      # 2. Redis & Elasticsearch
      - SPRING_DATA_REDIS_HOST=${redis_private_ip}
      - SPRING_DATA_REDIS_PORT=6379
      - ELASTICSEARCH_HOST=${es_private_ip}
      - ELASTICSEARCH_PORT=9200
      - ELASTICSEARCH_ENABLED=true

      # 3. Application Profiles & Security
      - SPRING_PROFILES_ACTIVE=prod
      - CUSTOM_JWT_SECRET_KEY=${jwt_secret_key}
      - SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KAKAO_CLIENT_ID=${kakao_client_id}
      - SERVER_FORWARD_HEADERS_STRATEGY=framework
      - CUSTOM_COOKIE_SECURE=true
      - CUSTOM_COOKIE_SAME_SITE=lax
      - APP_FRONTEND_URL=https://${dns_name}

      # 4. External API Keys
      - CUSTOM_API_ESTATE_KEY=${api_key_estate}
      - CUSTOM_API_ESTATE_URL=${api_url_estate}
      - CUSTOM_API_POLICY_KEY=${api_key_policy}
      - CUSTOM_API_POLICY_URL=${api_url_policy}
      - CUSTOM_API_GEO_KEY=${api_key_geo}
      - CUSTOM_API_GEO_URL=${api_url_geo}
      - CUSTOM_API_CENTER_KEY=${api_key_center}
      - CUSTOM_API_CENTER_URL=${api_url_center}
EOT

# 5. 실행
cd /home/ubuntu/app && docker-compose up -d
