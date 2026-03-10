#!/bin/bash
# scripts/setup_nginx_server.sh

# 1. 패키지 설치 및 기본 보안
sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
sudo systemctl start docker && sudo systemctl enable docker && sudo usermod -aG docker ubuntu

sudo sed -i 's/^#\?PasswordAuthentication .*/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart ssh

# Swap 설정 (SC2024 해결: tee -a)
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab > /dev/null

# 2. 인증서 경로 및 주입
CERT_DIR="/home/ubuntu/app/certbot/conf/live/${dns_name}"
sudo mkdir -p "$CERT_DIR"
sudo mkdir -p /home/ubuntu/app/nginx/conf.d

cat <<EOT | sudo tee "$CERT_DIR/fullchain.pem" > /dev/null
${fullchain_content}
EOT

cat <<EOT | sudo tee "$CERT_DIR/privkey.pem" > /dev/null
${privkey_content}
EOT

sudo chmod 600 "$CERT_DIR/privkey.pem"

# 3. Nginx Upstream 설정
cat <<EOT | sudo tee /home/ubuntu/app/nginx/conf.d/upstreams.inc > /dev/null
upstream was_frontend {
    server ${was_1_ip}:3000 max_fails=3 fail_timeout=30s;
    server ${was_2_ip}:3000 max_fails=3 fail_timeout=30s;
}

upstream was_backend {
    server ${was_1_ip}:8080 max_fails=3 fail_timeout=30s;
    server ${was_2_ip}:8080 max_fails=3 fail_timeout=30s;
}
EOT

# 4. Nginx 메인 설정 (빠졌던 헤더들 모두 복구 및 통합)
cat <<EOT | sudo tee /home/ubuntu/app/nginx/conf.d/default.conf > /dev/null
include /etc/nginx/conf.d/upstreams.inc;

server {
    listen 80;
    server_name ${dns_name};
    location /.well-known/acme-challenge/ {
        root /var/www/certbot;
    }
    return 301 https://\$host\$request_uri;
}

server {
    listen 443 ssl;
    server_name ${dns_name};

    ssl_certificate /etc/letsencrypt/live/${dns_name}/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/${dns_name}/privkey.pem;

    # 공통 프록시 헤더 설정 (반복 줄이기 위해 설정 가능하지만, 명확성을 위해 명시)
    location /oauth2 {
        proxy_pass http://was_backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Port \$server_port;
    }

    location /login/oauth2 {
        proxy_pass http://was_backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Port \$server_port;
    }

    location /api {
        proxy_pass http://was_backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
    }

    location / {
        proxy_pass http://was_frontend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header X-Forwarded-Port \$server_port;
        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
    }

    location /grafana/ {
        proxy_pass http://${monitor_private_ip}:3001;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOT

# 5. Docker Compose
cat <<EOT | sudo tee /home/ubuntu/app/docker-compose.yml > /dev/null
version: '3.8'
services:
  nginx:
    restart: unless-stopped
    image: nginx:latest
    container_name: nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ./certbot/conf:/etc/letsencrypt
      - ./certbot/www:/var/www/certbot
  certbot:
    restart: unless-stopped
    image: certbot/certbot:latest
    volumes:
      - ./certbot/conf:/etc/letsencrypt
      - ./certbot/www:/var/www/certbot
    entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait \$\$!; done;'"
EOT

cd /home/ubuntu/app && sudo docker-compose up -d
