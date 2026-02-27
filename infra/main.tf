# 1. AWS 리전 설정
provider "aws" {
  region = "ap-northeast-2" # 서울
}

# 2. 서버 이름,key 정의
variable "server_names" {
  default = ["nginx", "was-1", "was-2", "my-sql", "redis", "elasticsearch"]
}

resource "aws_key_pair" "deployer" {
  key_name   = "my-key"
  public_key = file("${path.module}/my-key.pub") # 내 컴퓨터의 공개키 경로
}


# 3. 보안 그룹 설정 (방화벽: 22번 포트 SSH 허용)
# [공통] SSH(22)는 내 IP에서만 열어두는 것이 좋으나, 테스트용으로 전체 개방
resource "aws_security_group" "ssh_sg" {
  name = "ssh-sg"

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# [Nginx] 외부에서 들어오는 80 포트 개방
resource "aws_security_group" "nginx_sg" {
  name = "nginx-sg"

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS (실제 암호화 통신용) - 이거 꼭 추가해야 함!
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # 배포/관리용 (SSH) - 키 인증 필수!
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# [WAS] Nginx 서버에서 오는 트래픽만 8080으로 허용 + 배포용 ssh
resource "aws_security_group" "was_sg" {
  name = "was-sg"
  # 1. 서비스 트래픽 (Nginx -> WAS)
  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.nginx_sg.id]
  }

  ingress {
      from_port       = 3000
      to_port         = 3000
      protocol        = "tcp"
      security_groups = [aws_security_group.nginx_sg.id]
    }

  # 2. 배포용 SSH (Nginx 서버에서만 접속 허용!)
  ingress {
    from_port       = 22
    to_port         = 22
    protocol        = "tcp"
    security_groups = [aws_security_group.nginx_sg.id] # 핵심: Nginx SG만 허용
  }

  # 3. 아웃바운드 규칙 (Docker 이미지를 받아오기 위해 필수)
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# [모니터링] 외부에서 Grafana(3001), Prometheus(9090) 접근 허용
resource "aws_security_group" "monitor_sg" {
  name = "monitor-sg"

  ingress {
    from_port   = 3001
    to_port     = 3001
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 9090
    to_port     = 9090
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# Nginx 서버를 위한 고정 IP(EIP) 할당
resource "aws_eip" "nginx_eip" {
  instance = aws_instance.nginx_server.id # nginx 인스턴스에 연결
  domain   = "vpc"

  tags = {
    Name = "nginx-fixed-ip"
  }
}

# 할당된 고정 IP 출력 (터미널에서 바로 확인용)
output "nginx_fixed_public_ip" {
  value = aws_eip.nginx_eip.public_ip
}

# [DB/Cache/Search] WAS 서버와 모니터링 서버에서만 접근 허용
resource "aws_security_group" "data_sg" {
  name = "data-sg"

  # MySQL
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [aws_security_group.was_sg.id, aws_security_group.monitor_sg.id]
  }

  # Redis
  ingress {
    from_port       = 6379
    to_port         = 6379
    protocol        = "tcp"
    security_groups = [aws_security_group.was_sg.id, aws_security_group.monitor_sg.id]
  }

  # Elasticsearch
  ingress {
    from_port       = 9200
    to_port         = 9200
    protocol        = "tcp"
    security_groups = [aws_security_group.was_sg.id, aws_security_group.monitor_sg.id]
  }

  # MySQL Exporter
  ingress {
    from_port       = 9104
    to_port         = 9104
    protocol        = "tcp"
    security_groups = [aws_security_group.monitor_sg.id]
  }

  # Redis Exporter
  ingress {
    from_port       = 9121
    to_port         = 9121
    protocol        = "tcp"
    security_groups = [aws_security_group.monitor_sg.id]
  }

  # ES Exporter
  ingress {
    from_port       = 9114
    to_port         = 9114
    protocol        = "tcp"
    security_groups = [aws_security_group.monitor_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}


# 4. 서버(EC2) 6대 생성
# 인프라 서버 생성 (데이터 계층) - 가장 먼저 띄워야 함
resource "aws_instance" "db_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.data_sg.id]
  tags = { Name = "db" }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
              sudo systemctl start docker && sudo usermod -aG docker ubuntu
              mkdir -p /home/ubuntu/app
              cat <<EOT > /home/ubuntu/app/docker-compose.yml
              version: '3.8'
              services:
                mysql:
                  image: mysql:latest
                  ports: [ "3306:3306" ]
                  environment:
                    - MYSQL_ROOT_PASSWORD=${var.db_password}
                    - MYSQL_DATABASE=my_db
                mysql-exporter:
                  image: prom/mysqld-exporter:latest
                  ports: [ "9104:9104" ]
                  environment:
                    - DATA_SOURCE_NAME=root:root_password@(mysql:3306)/
              EOT
              cd /home/ubuntu/app && docker-compose up -d
              EOF
}

resource "aws_instance" "redis_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.data_sg.id]
  tags = { Name = "redis" }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
              sudo systemctl start docker && sudo usermod -aG docker ubuntu
              mkdir -p /home/ubuntu/app
              cat <<EOT > /home/ubuntu/app/docker-compose.yml
              version: '3.8'
              services:
                redis:
                  image: redis:latest
                  ports: [ "6379:6379" ]
                redis-exporter:
                  image: oliver006/redis_exporter:latest
                  ports: [ "9121:9121" ]
                  environment:
                    - REDIS_ADDR=redis:6379
              EOT
              cd /home/ubuntu/app && docker-compose up -d
              EOF
}

resource "aws_instance" "es_server" {
  ami                    = "ami-04d25ae66444b2b10"
  instance_type          = "t3.micro"
  key_name               = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.data_sg.id]
  tags = { Name = "elasticsearch" }

  user_data = <<-EOF
              #!/bin/bash

              # 스왑 설정
              fallocate -l 2G /swapfile
              chmod 600 /swapfile
              mkswap /swapfile
              swapon /swapfile
              echo '/swapfile none swap sw 0 0' >> /etc/fstab

              # 패키지 설치
              apt-get update -y && apt-get install -y docker.io docker-compose

              # Docker 시작
              systemctl start docker
              systemctl enable docker
              usermod -aG docker ubuntu

              # ES 필수 커널 설정
              sysctl -w vm.max_map_count=262144
              echo 'vm.max_map_count=262144' >> /etc/sysctl.conf

              # 작업 디렉토리 생성
              mkdir -p /home/ubuntu/app

              # docker-compose.yml 생성
              cat <<EOT > /home/ubuntu/app/docker-compose.yml
              version: '3.8'
              services:
                elasticsearch:
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
                  restart: always

                elasticsearch-exporter:
                  image: prometheuscommunity/elasticsearch-exporter:latest
                  container_name: elasticsearch-exporter
                  ports:
                    - "9114:9114"
                  command:
                    - "--es.uri=http://elasticsearch:9200"
                  restart: always

              volumes:
                es-data:
              EOT

              # 실행
              cd /home/ubuntu/app && docker-compose up -d
              EOF
}

# WAS 서버 2대 생성 (App) - DB 서버들 IP 주입
resource "aws_instance" "was_servers" {
  count         = 2
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.was_sg.id]
  tags = { Name = "was-${count.index + 1}" }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
              sudo systemctl start docker && sudo usermod -aG docker ubuntu

              mkdir -p /home/ubuntu/app
              cat <<EOT > /home/ubuntu/app/docker-compose.yml
              version: '3.8'
              services:
                app:
                  image: gurum505/spring-next-app:v1.1 # Docker Hub에 올려둔 이미지
                  ports: [ "8080:8080" , "3000:3000" ]
                  environment:
                    # 1. Database
                    - SPRING_DATASOURCE_URL=jdbc:mysql://${aws_instance.db_server.private_ip}:3306/my_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul
                    - SPRING_DATASOURCE_USERNAME=${var.db_username}
                    - SPRING_DATASOURCE_PASSWORD=${var.db_password}
                    - SPRING_DATASOURCE_DRIVER_CLASS_NAME=${var.db_driver_class_name}

                    # 2. Redis & Elasticsearch
                    - SPRING_DATA_REDIS_HOST=${aws_instance.redis_server.private_ip}
                    - SPRING_DATA_REDIS_PORT=6379
                    - ELASTICSEARCH_HOST=${aws_instance.es_server.private_ip}
                    - ELASTICSEARCH_PORT=9200
                    - ELASTICSEARCH_ENABLED=true

                    # 3. Application Profiles & Security
                    # 기본값이 dev이기때문에 prod로 변경필요
                    - SPRING_PROFILES_ACTIVE=prod
                    - CUSTOM_JWT_SECRET_KEY=${var.jwt_secret_key}
                    - SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_KAKAO_CLIENT_ID=${var.kakao_client_id}

                    # 4. External API Keys (YAML의 구조에 맞춰 주입)
                    - CUSTOM_API_ESTATE_KEY=${var.api_key_estate}
                    - CUSTOM_API_ESTATE_URL=${var.api_url_estate}
                    - CUSTOM_API_POLICY_KEY=${var.api_key_policy}
                    - CUSTOM_API_POLICY_URL=${var.api_url_policy}
                    - CUSTOM_API_GEO_KEY=${var.api_key_geo}
                    - CUSTOM_API_GEO_URL=${var.api_url_geo}
                    - CUSTOM_API_CENTER_KEY=${var.api_key_center}
                    - CUSTOM_API_CENTER_URL=${var.api_url_center}

              EOT
              cd /home/ubuntu/app && docker-compose up -d
              EOF
}

# Nginx 서버  - WAS 서버 2대 IP 주입 (로드 밸런싱)
resource "aws_instance" "nginx_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.nginx_sg.id]
  tags = { Name = "nginx" }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
              sudo systemctl start docker && sudo usermod -aG docker ubuntu

              # 보안강화: SSH 비밀번호 로그인 차단 (키 인증만 허용)
              sudo sed -i 's/^#\?PasswordAuthentication .*/PasswordAuthentication no/' /etc/ssh/sshd_config
              sudo sed -i 's/^#\?PermitEmptyPasswords .*/PermitEmptyPasswords no/' /etc/ssh/sshd_config
              sudo systemctl restart ssh

              # 인증서 파일공유
              mkdir -p /home/ubuntu/app
              mkdir -p /home/ubuntu/app/certbot/conf
              mkdir -p /home/ubuntu/app/certbot/www

              # Nginx 설정 파일 자동 생성 (WAS 1, 2 로드밸런싱 설정)
              cat <<EOT > /home/ubuntu/app/nginx.conf
              upstream was_frontend {
                  server ${aws_instance.was_servers[0].private_ip}:3000 max_fails=3 fail_timeout=30s;
                  server ${aws_instance.was_servers[1].private_ip}:3000 max_fails=3 fail_timeout=30s;
              }

              upstream was_backend {
                  server ${aws_instance.was_servers[0].private_ip}:8080 max_fails=3 fail_timeout=30s;
                  server ${aws_instance.was_servers[1].private_ip}:8080 max_fails=3 fail_timeout=30s;
              }

              server {
                  listen 80;
                  server_name gurum505.duckdns.org;

                  # 1. Certbot 인증 경로 (매우 중요)
                  location /.well-known/acme-challenge/ {
                      root /var/www/certbot;
                  }

                  # 공통 프록시 설정 (가독성을 위해 블록 밖으로 뺄 수 없으므로 각 location에 적용)
                  # 2. API 요청 (Spring Boot)
                  location /api {
                      proxy_pass http://was_backend;
                      proxy_set_header Host \$host;
                      proxy_set_header X-Real-IP \$remote_addr;
                      proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;

                      proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
                      proxy_connect_timeout 5s;
                      proxy_read_timeout 60s;
                  }

                  # 3. 정적 파일 및 화면 요청 (Next.js)
                  location / {
                      proxy_pass http://was_frontend;
                      proxy_set_header Host \$host;
                      proxy_set_header X-Real-IP \$remote_addr;
                      proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;

                      proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
                      proxy_connect_timeout 5s;
                      proxy_read_timeout 60s;
                  }

                  # 그 외 일반 접속은 모두 HTTPS로 강제 이동 (301 리다이렉트)
                  #location / {
                  #    return 301 https://$host$request_uri;
                  #}


              }

              # [참고] 인증서 발급이 완료된 후, 아래의 443 서버 블록을 주석 해제하고
              # 위 80 포트 블록의 location / 및 /api 프록시를 이쪽으로 옮기면 완벽한 HTTPS가 됩니다.
              # server {
              #     listen 443 ssl;
              #     server_name gurum505.duckdns.org;
              #     ssl_certificate /etc/letsencrypt/live/gurum505.duckdns.org/fullchain.pem;
              #     ssl_certificate_key /etc/letsencrypt/live/gurum505.duckdns.org/privkey.pem;
              # 공통 프록시 설정 (가독성을 위해 블록 밖으로 뺄 수 없으므로 각 location에 적용)
#                     # 2. API 요청 (Spring Boot)
#                     location /api {
#                         proxy_pass http://was_backend;
#                         proxy_set_header Host \$host;
#                         proxy_set_header X-Real-IP \$remote_addr;
#                         proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
#
#                         proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
#                         proxy_connect_timeout 5s;
#                         proxy_read_timeout 60s;
#                     }
#
#                     # 3. 정적 파일 및 화면 요청 (Next.js)
#                     location / {
#                         proxy_pass http://was_frontend;
#                         proxy_set_header Host \$host;
#                         proxy_set_header X-Real-IP \$remote_addr;
#                         proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
#
#                         proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
#                         proxy_connect_timeout 5s;
#                         proxy_read_timeout 60s;
#                     }
              # }
              EOT

              cat <<EOT > /home/ubuntu/app/docker-compose.yml
              version: '3.8'
              services:
                nginx:
                  image: nginx:latest
                  ports:
                    - "80:80"
                    - "443:443" # [수정] HTTPS용 포트 개방
                  volumes:
                    - ./nginx.conf:/etc/nginx/conf.d/default.conf:ro
                    - ./certbot/conf:/etc/letsencrypt  # [수정] Certbot이 발급한 인증서를 Nginx와 공유
                    - ./certbot/www:/var/www/certbot   # [수정] Certbot의 Challenge 폴더를 Nginx와 공유
                certbot:
                  image: certbot/certbot:latest
                  volumes:
                    - ./certbot/conf:/etc/letsencrypt
                    - ./certbot/www:/var/www/certbot
                  entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"
              EOT
              cd /home/ubuntu/app && docker-compose up -d
              EOF
}

# 모니터링 서버 (Prometheus + Grafana) - 모든 데이터 IP 주입
resource "aws_instance" "monitor_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.monitor_sg.id]
  tags = { Name = "monitoring" }

  user_data = <<-EOF
              #!/bin/bash
              sudo apt-get update -y && sudo apt-get install -y docker.io docker-compose
              sudo systemctl start docker && sudo usermod -aG docker ubuntu
              mkdir -p /home/ubuntu/app

              cat <<EOT > /home/ubuntu/app/prometheus.yml
              global:
                scrape_interval: 15s
              scrape_configs:
                - job_name: 'mysql'
                  static_configs:
                    - targets: ['${aws_instance.db_server.private_ip}:9104']
                - job_name: 'redis'
                  static_configs:
                    - targets: ['${aws_instance.redis_server.private_ip}:9121']
                - job_name: 'elasticsearch'
                  static_configs:
                    - targets: ['${aws_instance.es_server.private_ip}:9114']
              EOT

              cat <<EOT > /home/ubuntu/app/docker-compose.yml
              version: '3.8'
              services:
                prometheus:
                  image: prom/prometheus:latest
                  ports: [ "9090:9090" ]
                  volumes:
                    - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
                grafana:
                  image: grafana/grafana:latest
                  ports: [ "3001:3000" ]
                  environment:
                    - GF_SECURITY_ADMIN_PASSWORD=admin
              EOT
              cd /home/ubuntu/app && docker-compose up -d
              EOF
}


# 5. 생성 완료 후 IP 주소 출력
output "nginx_public_ip" {
  value = aws_instance.nginx_server.public_ip
  description = "웹 서비스 접속 주소 (이 IP를 브라우저에 입력하세요)"
}

output "grafana_public_ip" {
  value = "${aws_instance.monitor_server.public_ip}:3001"
  description = "모니터링 대시보드 주소"
}

output "was_private_ips" {
  value = aws_instance.was_servers[*].private_ip
  description = "주요 WAS Private IP 주소"
}

