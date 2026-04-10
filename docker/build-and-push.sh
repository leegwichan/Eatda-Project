#!/bin/bash
set -e

REGISTRY="keochan"
TAG="${1:-latest}"

echo "=== Building and pushing Eatda Docker images (tag: $TAG) ==="

# 1. Server: 기존 Dockerfile로 베이스 빌드 → init.sql 레이어 추가
echo ""
echo ">>> [1/3] Building server image..."
docker build -f Dockerfile.server -t eatda-server-base ../server
echo "FROM eatda-server-base
COPY seed/db/init.sql /app/seed/init.sql" | docker build -t "$REGISTRY/eatda-server:$TAG" -f - .

# 2. LocalStack: 시드 데이터 내장
echo ""
echo ">>> [2/3] Building localstack image..."
docker build -f Dockerfile.localstack -t "$REGISTRY/eatda-localstack:$TAG" .

# 3. Web: 그대로 빌드 (빌드 args 기본값 사용)
echo ""
echo ">>> [3/3] Building web image..."
docker build -f Dockerfile.web \
  --build-arg NEXT_PUBLIC_API_URL=http://localhost:8080 \
  --build-arg NEXT_PUBLIC_DEV_LOGIN_ENABLED=true \
  -t "$REGISTRY/eatda-web:$TAG" ../web

# 4. Push
echo ""
echo ">>> Pushing images to Docker Hub..."
docker push "$REGISTRY/eatda-server:$TAG"
docker push "$REGISTRY/eatda-localstack:$TAG"
docker push "$REGISTRY/eatda-web:$TAG"

echo ""
echo "=== Done! All images pushed as $REGISTRY/eatda-*:$TAG ==="
