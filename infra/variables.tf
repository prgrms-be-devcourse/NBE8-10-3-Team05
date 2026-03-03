# --- docker ---
variable "docker_image_name" {
  description = "도커 이미지"
  type        = string
  default     = "root"
}

# --- github credentials ---
variable "github_username" {
  description = "GitHub 사용자 이름"
  type        = string
}

variable "github_token" {
  description = "GitHub Personal Access Token (read:packages 권한 필수)"
  type        = string
  sensitive   = true # 터미널 출력 시 값을 숨깁니다.
}

# --- dns ---
variable "dns_name" {
  description = "dns 서버 주소"
  type        = string
  default     = "root"
}

# --- Database Credentials ---
variable "db_username" {
  description = "MySQL 접속 계정"
  type        = string
  default     = "root"
}

variable "db_password" {
  description = "MySQL 접속 비밀번호"
  type        = string
  sensitive   = true
}

variable "db_driver_class_name" {
  description = "db_driver_class_name"
    type        = string
    sensitive   = true
}

# --- OAuth ---
variable "kakao_client_id" {
  type      = string
  sensitive = true
}

# --- Custom API Keys ---
variable "api_key_estate" {
  description = "국토교통부 마이홈포털 API Key"
  type        = string
  sensitive   = true
}

variable "api_key_policy" {
  description = "온라인 청년센터 API Key"
  type        = string
  sensitive   = true
}

variable "api_key_geo" {
  description = "카카오 로컬 API Key"
  type        = string
  sensitive   = true
}

variable "api_key_center" {
  description = "공공데이터포털 센터 API Key"
  type        = string
  sensitive   = true
}

variable "api_url_estate" {
  description = "국토교통부 마이홈포털 API url"
  type        = string
  sensitive   = true
}

variable "api_url_policy" {
  description = "온라인 청년센터 API url"
  type        = string
  sensitive   = true
}

variable "api_url_geo" {
  description = "카카오 로컬 API url"
  type        = string
  sensitive   = true
}

variable "api_url_center" {
  description = "공공데이터포털 센터 API url"
  type        = string
  sensitive   = true
}

# --- JWT ---
variable "jwt_secret_key" {
  description = "JWT Secret Key for Token Signing"
  type        = string
  sensitive   = true
}
