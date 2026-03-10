# 4. 서버(EC2) 6대 생성
# 인프라 서버 생성 (데이터 계층) - 가장 먼저 띄워야 함
resource "aws_instance" "db_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.data_sg.id]
  tags = { Name = "db" }
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/scripts/setup_db_server.sh", {
    db_password = var.db_password
  })
}

resource "aws_instance" "redis_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.data_sg.id]
  tags = { Name = "redis" }
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/scripts/setup_redis_server.sh", {})
}

resource "aws_instance" "es_server" {
  ami                    = "ami-04d25ae66444b2b10"
  instance_type          = "t3.micro"
  key_name               = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.data_sg.id]
  tags = { Name = "elasticsearch" }
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/scripts/setup_es_server.sh", {})
}

# WAS 서버 2대 생성 (App) - DB 서버들 IP 주입
resource "aws_instance" "was_servers" {
  count         = 2
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.was_sg.id]
  tags = { Name = "was-${count.index + 1}" }
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/scripts/setup_was_server.sh", {
    # GitHub & Docker
    github_token      = var.github_token
    github_username   = var.github_username
    docker_image_name = var.docker_image_name

    # Infra IPs (동적 참조)
    db_private_ip     = aws_instance.db_server.private_ip
    redis_private_ip  = aws_instance.redis_server.private_ip
    es_private_ip     = aws_instance.es_server.private_ip

    # Credentials & Config
    db_username       = var.db_username
    db_password       = var.db_password
    db_driver_class_name = var.db_driver_class_name
    jwt_secret_key    = var.jwt_secret_key
    kakao_client_id   = var.kakao_client_id
    dns_name          = var.dns_name

    # External APIs
    api_key_estate    = var.api_key_estate
    api_url_estate    = var.api_url_estate
    api_key_policy    = var.api_key_policy
    api_url_policy    = var.api_url_policy
    api_key_geo       = var.api_key_geo
    api_url_geo       = var.api_url_geo
    api_key_center    = var.api_key_center
    api_url_center    = var.api_url_center
  })
}

# Nginx 서버  - WAS 서버 2대 IP 주입 (로드 밸런싱)
resource "aws_instance" "nginx_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.nginx_sg.id]
  tags = { Name = "nginx" }
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/scripts/setup_nginx_server.sh", {
    dns_name           = var.dns_name
    # 파일 내용을 읽어서 전달
    fullchain_content  = file("${path.module}/domain/fullchain.pem")
    privkey_content    = file("${path.module}/domain/privkey.pem")
    # WAS 서버들의 사설 IP (리스트 인덱스 참조)
    was_1_ip           = aws_instance.was_servers[0].private_ip
    was_2_ip           = aws_instance.was_servers[1].private_ip
    # 모니터링 서버 IP
    monitor_private_ip = aws_instance.monitor_server.private_ip
  })
}

# 모니터링 서버 (Prometheus + Grafana) - 모든 데이터 IP 주입
resource "aws_instance" "monitor_server" {
  ami           = "ami-04d25ae66444b2b10"
  instance_type = "t3.micro"
  key_name      = aws_key_pair.deployer.key_name
  vpc_security_group_ids = [aws_security_group.ssh_sg.id, aws_security_group.monitor_sg.id]
  tags = { Name = "monitoring" }
  user_data_replace_on_change = true

  user_data = templatefile("${path.module}/scripts/setup_monitor_server.sh", {
    dns_name          = var.dns_name
    db_private_ip     = aws_instance.db_server.private_ip
    redis_private_ip  = aws_instance.redis_server.private_ip
    es_private_ip     = aws_instance.es_server.private_ip
    was_1_private_ip  = aws_instance.was_servers[0].private_ip
    was_2_private_ip  = aws_instance.was_servers[1].private_ip
  })
}


# 5. 생성 완료 후 IP 주소 출력
output "nginx_public_ip" {
  value = aws_instance.nginx_server.public_ip
  description = "웹 서비스 접속 주소 (이 IP를 브라우저에 입력하세요)"
}

output "monitor_public_ip" {
  value = "${aws_instance.monitor_server.public_ip}:3001"
  description = "모니터링 대시보드 주소"
}

output "server_access_summary" {
  value = {
    nginx   = { public = aws_eip.nginx_eip.public_ip, private = aws_instance.nginx_server.private_ip }
    monitor = { public = aws_instance.monitor_server.public_ip, private = aws_instance.monitor_server.private_ip }
    was_1   = { public = aws_instance.was_servers[0].public_ip, private = aws_instance.was_servers[0].private_ip }
    was_2   = { public = aws_instance.was_servers[1].public_ip, private = aws_instance.was_servers[1].private_ip }
    db      = { public = aws_instance.db_server.public_ip, private = aws_instance.db_server.private_ip }
  }
  description = "전체 서버 IP 주소 요약표"
}

resource "null_resource" "post_deploy_update" {
  # EIP가 할당/변경될 때마다 실행되도록 트리거 설정
  triggers = {
    eip_ip = aws_eip.nginx_eip.public_ip
    was1_ip  = aws_instance.was_servers[0].private_ip
    was2_ip  = aws_instance.was_servers[1].private_ip
  }

  provisioner "local-exec" {
    interpreter = ["bash", "-c"] # window에서도 실행가능하도록
    command = "chmod +x ./update_infrastructure.sh && bash ./update_infrastructure.sh"

    environment = {
      DUCKDNS_DOMAIN = var.dns_name       # 본인의 DuckDNS 도메인 (예: gurum505)
      DUCKDNS_TOKEN  = var.dns_token    # DuckDNS 토큰

      GH_REPO        = var.github_repo        # GitHub 저장소 (예: sayhojeong505/my-project)
      NGINX_HOST    = aws_eip.nginx_eip.public_ip           # GitHub에 저장될 시크릿 이름
      WAS1_PRIVATE_IP = aws_instance.was_servers[0].private_ip
      WAS2_PRIVATE_IP = aws_instance.was_servers[1].private_ip
    }
  }

  # EIP와 Nginx 인스턴스가 완전히 준비된 후 실행
  depends_on = [aws_eip.nginx_eip, aws_instance.nginx_server]
}
