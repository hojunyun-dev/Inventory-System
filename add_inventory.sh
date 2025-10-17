#!/bin/bash

# 재고 데이터 추가 스크립트
BASE_URL="http://localhost:8080/api"
AUTH="Authorization: Basic dXNlcjpwYXNzd29yZA=="

echo "재고 데이터 추가 시작..."

# 제품 ID 1-8에 대해 재고 데이터 생성
for i in {1..8}; do
    QUANTITY=$((30 + RANDOM % 70))
    LOCATION="A-0$((i%3+1))-0$((i%2+1))"
    
    echo "제품 ID $i에 재고 추가 중... (수량: $QUANTITY, 위치: $LOCATION)"
    
    curl -s -X POST "$BASE_URL/inventories" \
        -H "$AUTH" \
        -H "Content-Type: application/json" \
        -d "{\"productId\": $i, \"quantity\": $QUANTITY, \"warehouseLocation\": \"$LOCATION\"}" \
        > /dev/null
    
    if [ $? -eq 0 ]; then
        echo "✅ 제품 ID $i 재고 추가 완료"
    else
        echo "❌ 제품 ID $i 재고 추가 실패"
    fi
    
    sleep 1
done

echo "재고 데이터 추가 완료!"
echo "재고 목록 확인:"
curl -s -X GET "$BASE_URL/inventories" -H "$AUTH" | jq 'length' 2>/dev/null || echo "재고 데이터 확인 중..."

