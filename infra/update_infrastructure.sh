#!/bin/bash

echo "--------------------------------------------------"
echo "🚀 [Post-Deploy] 업데이트 시작..."

# 1. GitHub CLI 로그인 상태 확인
echo "🔍 GitHub CLI 로그인 상태 확인 중..."
if ! gh auth status &>/dev/null; then
    echo "⚠️  경고: GitHub CLI에 로그인되어 있지 않습니다!"
    echo "👉 'gh auth login'을 완료한 후 다시 시도하세요."
    GH_LOGGED_IN=false
else
    echo "✅ GitHub CLI 로그인 확인 완료."
    GH_LOGGED_IN=true
fi

# 2. 데이터 가공 (Terraform에서 이미 파싱해서 넘겼더라도 안전을 위해 한 번 더 처리)
# "nbe8-10-3-team05.duckdns.org" -> "nbe8-10-3-team05"
DOMAIN_ONLY=$(echo "$DUCKDNS_DOMAIN" | sed 's/\.duckdns\.org//')

# "https://github.com/owner/repo.git" -> "owner/repo"
CLEAN_REPO=$(echo "$GH_REPO" | sed 's|https://github.com/||' | sed 's|http://github.com/||' | sed 's|\.git$||')

# 3. DuckDNS 업데이트
echo "🦆 DuckDNS($DOMAIN_ONLY) 갱신 중... (IP: $NGINX_HOST)"
curl -s "https://www.duckdns.org/update?domains=${DOMAIN_ONLY}&token=${DUCKDNS_TOKEN}&ip=${NGINX_HOST}"
echo -e "\n✅ DuckDNS 업데이트 요청 완료."

# 4. GitHub Secrets 업데이트 (로그인 된 경우만)
if [ "$GH_LOGGED_IN" = true ]; then
    echo "🔑 GitHub Secrets 업데이트 중 ($CLEAN_REPO)..."
    # 변수 앞에 $를 꼭 붙여야 합니다!
    echo "$NGINX_HOST" | gh secret set "NGINX_HOST" --repo "$CLEAN_REPO"
    echo "$WAS1_PRIVATE_IP" | gh secret set "WAS1_PRIVATE_IP" --repo "$CLEAN_REPO"
    echo "$WAS2_PRIVATE_IP" | gh secret set "WAS2_PRIVATE_IP" --repo "$CLEAN_REPO"
    echo "✅ 모든 GitHub Secrets 업데이트 완료!"
else
    echo "❌ GitHub 로그인이 되어 있지 않아 Secrets 업데이트를 건너뜁니다."
fi

echo "--------------------------------------------------"
