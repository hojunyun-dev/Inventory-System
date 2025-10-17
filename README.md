# 🚗 자동차 부품 재고 관리 시스템

다양한 온라인 마켓에 등록된 자동차 부품 재고를 한 번에 관리하는 마이크로서비스 아키텍처 기반 시스템입니다.

## 📋 프로젝트 개요

### 핵심 기능
- **GPARTS 카탈로그 연동**: 자동차 부품 정보 표준화
- **다중 플랫폼 연동**: 네이버, 쿠팡, 카페24, 옥션, 번개장터, 당근마켓, 중고나라
- **실시간 재고 동기화**: 입고, 출고, 품절, 이동 등 모든 재고 변동 실시간 반영
- **통합 대시보드**: 각 플랫폼별 상품 코드, 재고 수량, 가격 변동 내역 관리

### 기대효과
- 여러 판매 채널에 흩어진 부품 정보를 **한 곳에서 관리**하여 업무 효율 극대화
- **재고 실시간 동기화**로 오판매(재고 없는 상품 판매) 방지
- **GPARTS 카탈로그 연동**으로 부품 코드, 차량 호환성 등 상품 정보 표준화
- 판매 이력, 수량, 가격 변동 등의 **데이터 기반 의사결정 지원**

## 🏗️ 아키텍처

### 마이크로서비스 구조
```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React)                        │
│                     Port: 3000                             │
└─────────────────────┬───────────────────────────────────────┘
                      │
┌─────────────────────┴───────────────────────────────────────┐
│                Backend Service                              │
│                  Port: 8080                                │
│              (재고 관리 시스템)                              │
└─────────┬─────────────────────────┬─────────────────────────┘
          │                         │
┌─────────▼─────────┐    ┌─────────▼─────────┐
│ Token Management  │    │ Registration      │
│    Service        │    │    Service        │
│   Port: 8081      │    │   Port: 8082      │
│ (토큰 관리)        │    │ (플랫폼 등록)      │
└───────────────────┘    └───────────────────┘
```

### 기술 스택
- **Backend**: Spring Boot 3.2.0, Java 17
- **Frontend**: React 18.2.0, TypeScript, Material-UI
- **Database**: H2 (개발용), MariaDB (운영용)
- **Communication**: REST API, WebClient
- **Container**: Docker, Kubernetes

## 🚀 시작하기

### 필수 요구사항
- Java 17+
- Node.js 16+
- Maven 3.6+
- Git

### 설치 및 실행

1. **저장소 클론**
```bash
git clone https://github.com/YOUR_USERNAME/inventory-system.git
cd inventory-system
```

2. **백엔드 실행**
```bash
# 메인 백엔드 서비스
cd backend
mvn spring-boot:run

# 토큰 관리 서비스
cd ../token-management-service
mvn spring-boot:run

# 등록 서비스
cd ../registration-service
mvn spring-boot:run
```

3. **프론트엔드 실행**
```bash
cd frontend
npm install
npm start
```

### 서비스 접속 정보
- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080
- **토큰 관리 서비스**: http://localhost:8081
- **등록 서비스**: http://localhost:8082
- **H2 콘솔**: http://localhost:8080/h2-console

### 기본 로그인 정보
- **사용자명**: admin
- **비밀번호**: admin123

## 📚 API 문서

### 주요 엔드포인트
- `GET /api/products` - 상품 목록 조회
- `POST /api/platform/{platform}/register` - 플랫폼별 상품 등록
- `GET /api/tokens/{platform}` - 플랫폼 토큰 조회
- `POST /api/registrations` - 상품 등록 요청

## 🔧 개발 가이드

### 프로젝트 구조
```
inventory-system/
├── backend/                    # 메인 재고관리 서비스
├── frontend/                   # React 프론트엔드
├── token-management-service/   # 토큰 관리 서비스
├── registration-service/       # 플랫폼 등록 서비스
└── docs/                      # 문서
```

### 개발 환경 설정
1. IDE 설정 (IntelliJ IDEA 권장)
2. Java 17 SDK 설정
3. Node.js 16+ 설치
4. Maven 의존성 설치

## 🧪 테스트

### 테스트 실행
```bash
# 백엔드 테스트
mvn test

# 프론트엔드 테스트
cd frontend
npm test
```

### 테스트 가이드
- **UI 테스트**: `UI_TEST_GUIDE.md` 참조 (185개 테스트 항목)
- **API 테스트**: `API_TEST_REPORT.md` 참조
- **통합 테스트**: `INTEGRATED_TEST_SCENARIOS.md` 참조

## 📈 배포

### Docker 배포
```bash
# 전체 서비스 빌드
docker-compose build

# 서비스 실행
docker-compose up -d
```

### Kubernetes 배포
```bash
# 네임스페이스 생성
kubectl create namespace inventory-system

# 배포 실행
kubectl apply -f k8s/
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 생성해 주세요.

---

**개발 상태**: 4단계 (재고관리 시스템 리팩토링) 진행 중  
**마지막 업데이트**: 2025년 10월 17일
