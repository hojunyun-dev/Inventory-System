# 환경 변수 설정 가이드

## 📋 개요
이 문서는 자동차 부품 재고 관리 시스템의 환경 변수 설정 방법을 설명합니다.

## 🔧 필수 환경 변수 설정

### 1. 데이터베이스 설정
```bash
# MariaDB 설정
DB_HOST=localhost
DB_PORT=3307
DB_USERNAME=inventory
DB_PASSWORD=inventory123

# 데이터베이스별 설정
TOKEN_DB_NAME=token_management_db
REGISTRATION_DB_NAME=registration_db
BACKEND_DB_NAME=inventory_system
```

### 2. JWT 및 보안 설정
```bash
# JWT 설정
JWT_SECRET=mySecretKey123456789012345678901234567890
JWT_EXPIRATION=86400000

# 암호화 설정
ENCRYPTION_KEY=myEncryptionKey123456789012345678901234567890
```

### 3. 플랫폼 API 설정

#### 네이버 커머스 API
```bash
NAVER_CLIENT_ID=your_naver_client_id_here
NAVER_CLIENT_SECRET=your_naver_client_secret_here
```

#### 카페24 API
```bash
CAFE24_CLIENT_ID=your_cafe24_client_id_here
CAFE24_CLIENT_SECRET=your_cafe24_client_secret_here
```

#### 쿠팡 API
```bash
COUPANG_ACCESS_KEY=your_coupang_access_key_here
COUPANG_SECRET_KEY=your_coupang_secret_key_here
```

#### GPARTS API
```bash
GPARTS_API_KEY=your_gparts_api_key_here
GPARTS_API_URL=https://api.gparts.co.kr
```

#### Ziparts API
```bash
ZIPARTS_API_KEY=your_ziparts_api_key_here
ZIPARTS_API_URL=https://api.ziparts.co.kr
```

### 4. 서비스 URL 설정
```bash
TOKEN_SERVICE_URL=http://localhost:8081
REGISTRATION_SERVICE_URL=http://localhost:8082
BACKEND_SERVICE_URL=http://localhost:8080
```

### 5. 자동화 설정
```bash
AUTOMATION_HEADLESS=true
AUTOMATION_TIMEOUT=30000
AUTOMATION_MAX_RETRIES=3
```

## 🚀 설정 방법

### 방법 1: 시스템 환경 변수 설정
```bash
# Linux/Mac
export NAVER_CLIENT_ID="your_actual_client_id"
export NAVER_CLIENT_SECRET="your_actual_client_secret"

# Windows
set NAVER_CLIENT_ID=your_actual_client_id
set NAVER_CLIENT_SECRET=your_actual_client_secret
```

### 방법 2: IDE 환경 변수 설정
- **IntelliJ IDEA**: Run Configuration > Environment Variables
- **VS Code**: .vscode/launch.json 파일에서 env 설정
- **Eclipse**: Run Configuration > Environment 탭

### 방법 3: application.yml 파일 직접 수정
각 서비스의 `src/main/resources/application.yml` 파일에서 직접 값 설정:

```yaml
oauth:
  naver:
    client-id: your_actual_client_id
    client-secret: your_actual_client_secret
```

## 🔍 API 키 발급 방법

### 네이버 커머스 API
1. [네이버 개발자 센터](https://developers.naver.com/) 접속
2. 애플리케이션 등록
3. Commerce API 선택
4. Client ID, Client Secret 발급

### 카페24 API
1. [카페24 개발자 센터](https://developers.cafe24.com/) 접속
2. 앱 등록
3. API 권한 설정
4. Client ID, Client Secret 발급

### 쿠팡 API
1. [쿠팡 파트너스](https://partners.coupang.com/) 접속
2. 파트너스 가입
3. API 키 발급

## ⚠️ 보안 주의사항

1. **절대 Git에 실제 API 키를 커밋하지 마세요**
2. **환경 변수 파일은 .gitignore에 추가하세요**
3. **운영 환경에서는 강력한 비밀번호를 사용하세요**
4. **API 키는 정기적으로 갱신하세요**

## 🧪 설정 확인

### 토큰 관리 서비스 테스트
```bash
curl http://localhost:8081/actuator/health
```

### 등록 서비스 테스트
```bash
curl http://localhost:8082/actuator/health
```

### 백엔드 서비스 테스트
```bash
curl http://localhost:8080/actuator/health
```

## 📞 문제 해결

### 환경 변수가 인식되지 않는 경우
1. IDE 재시작
2. 터미널 재시작
3. 환경 변수 경로 확인

### API 연결 오류가 발생하는 경우
1. API 키 유효성 확인
2. 네트워크 연결 상태 확인
3. 방화벽 설정 확인

## 📚 참고 자료
- [Spring Boot Externalized Configuration](https://spring.io/guides/gs/spring-boot-config/)
- [Environment Variables Best Practices](https://12factor.net/config)
