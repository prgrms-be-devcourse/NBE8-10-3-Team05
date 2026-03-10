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
