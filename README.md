# 🚗 자동차 부품 재고 관리 시스템

다양한 온라인 마켓에 등록된 자동차 부품 재고를 한 번에 관리하는 마이크로서비스 아키텍처 기반 시스템입니다.

**현재 상태**: 개발 완료 및 배포 준비 단계 (2025년 1월 25일)

## 📋 프로젝트 개요

### 핵심 기능

* **GPARTS 카탈로그 연동**: 자동차 부품 정보 표준화
* **다중 플랫폼 연동**: 네이버, 쿠팡, 카페24, 옥션, 번개장터, 당근마켓, 중고나라
* **실시간 재고 동기화**: 입고, 출고, 품절, 이동 등 모든 재고 변동 실시간 반영
* **통합 대시보드**: 각 플랫폼별 상품 코드, 재고 수량, 가격 변동 내역 관리
* **배송 라벨 관리**: 배송 라벨 생성 및 배치 업데이트 기능
* **고급 검색**: 차량 호환성 기반 부품 검색 및 필터링

### 기대효과

* 여러 판매 채널에 흩어진 부품 정보를 **한 곳에서 관리**하여 업무 효율 극대화
* **재고 실시간 동기화**로 오판매(재고 없는 상품 판매) 방지
* **GPARTS 카탈로그 연동**으로 부품 코드, 차량 호환성 등 상품 정보 표준화
* 판매 이력, 수량, 가격 변동 등의 **데이터 기반 의사결정 지원**

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

* **Backend**: Spring Boot 3.2.0, Java 17
* **Frontend**: React 18.2.0, TypeScript, Material-UI
* **Database**: H2 (개발용), MariaDB (운영용)
* **Communication**: REST API, WebClient
* **Container**: Docker, Kubernetes

## 🚀 시작하기

### 필수 요구사항

* Java 17+
* Node.js 16+
* Maven 3.6+
* Git

### 설치 및 실행

1. **저장소 클론**

```bash
git clone https://github.com/hojunyun-dev/Inventory-System.git
cd Inventory-System
```

2. **백엔드 실행**

```bash
# 메인 백엔드 서비스
cd backend
mvn spring-boot:run

# 토큰 관리 서비스 (새 터미널)
cd ../token-management-service
mvn spring-boot:run

# 등록 서비스 (새 터미널)
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

* **프론트엔드**: http://localhost:3000
* **백엔드 API**: http://localhost:8080
* **토큰 관리 서비스**: http://localhost:8081
* **등록 서비스**: http://localhost:8082
* **H2 콘솔**: http://localhost:8080/h2-console

### 기본 로그인 정보

* **사용자명**: admin
* **비밀번호**: admin123

## 📚 API 문서

### 주요 엔드포인트

* `GET /api/products` - 상품 목록 조회
* `POST /api/platform/{platform}/register` - 플랫폼별 상품 등록
* `GET /api/tokens/{platform}` - 플랫폼 토큰 조회
* `POST /api/registrations` - 상품 등록 요청
* `GET /api/inventory` - 재고 목록 조회
* `POST /api/inventory` - 재고 등록
* `GET /api/vehicle-compatibility` - 차량 호환성 조회
* `POST /api/shipping-labels` - 배송 라벨 생성

## 🔧 개발 가이드

### 현재 프로젝트 구조

```
inventory-system/
├── backend/                    # 메인 재고관리 서비스
│   ├── src/main/java/com/inventory/    # Java 소스 코드
│   ├── target/                 # 컴파일된 클래스 파일
│   ├── data/                   # H2 데이터베이스 파일
│   └── pom.xml                 # Maven 설정
├── frontend/                   # React 프론트엔드
│   ├── src/                    # TypeScript 소스 코드
│   │   ├── components/         # 재사용 가능한 컴포넌트
│   │   ├── pages/              # 페이지 컴포넌트
│   │   ├── services/           # API 서비스
│   │   └── types/              # TypeScript 타입 정의
│   ├── build/                  # 빌드된 정적 파일
│   ├── node_modules/           # NPM 의존성
│   └── package.json            # NPM 설정
├── token-management-service/   # 토큰 관리 서비스
│   ├── src/main/java/          # Java 소스 코드
│   └── target/                 # 컴파일된 클래스 파일
├── registration-service/       # 플랫폼 등록 서비스
│   ├── src/main/java/          # Java 소스 코드
│   └── target/                 # 컴파일된 클래스 파일
├── docker-compose.yml          # Docker Compose 설정
├── .gitignore                  # Git 제외 파일 설정
└── README.md                   # 프로젝트 문서
```

### 주요 기능 모듈

#### 프론트엔드 페이지
- **Dashboard**: 통합 대시보드
- **InventoryList**: 재고 목록 관리
- **InventoryForm**: 재고 등록/수정
- **ProductList**: 상품 목록
- **ProductForm**: 상품 등록/수정
- **ProductDetail**: 상품 상세 정보
- **SerializedInventoryList**: 시리얼화된 재고 관리
- **VehicleCompatibilityList**: 차량 호환성 관리
- **VehiclePartSearch**: 차량 부품 검색
- **OrderHistory**: 주문 이력
- **MessageList**: 메시지 관리
- **ShippingLabel**: 배송 라벨 관리
- **ChannelProductManagement**: 채널별 상품 관리

#### 플랫폼 연동
- **NaverIntegration**: 네이버 스마트스토어 연동
- **CoupangIntegration**: 쿠팡 연동
- **Cafe24Integration**: 카페24 연동
- **GpartsIntegration**: GPARTS 카탈로그 연동
- **ZipartsIntegration**: Ziparts 연동

#### 백엔드 서비스
- **InventoryController**: 재고 관리 API
- **ProductController**: 상품 관리 API
- **TokenController**: 토큰 관리 API
- **RegistrationController**: 등록 서비스 API
- **VehicleCompatibilityService**: 차량 호환성 서비스

### 개발 환경 설정

1. **IDE 설정** (IntelliJ IDEA 권장)
2. **Java 17 SDK** 설정
3. **Node.js 16+** 설치
4. **Maven 의존성** 설치

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

* **UI 테스트**: `UI_TEST_GUIDE.md` 참조 (185개 테스트 항목)
* **API 테스트**: `API_TEST_REPORT.md` 참조
* **통합 테스트**: `INTEGRATED_TEST_SCENARIOS.md` 참조
* **빠른 테스트 체크리스트**: `QUICK_TEST_CHECKLIST.md` 참조

### 테스트 시나리오

#### 재고 관리 테스트
1. 재고 등록 및 수정
2. 재고 목록 조회 및 필터링
3. 시리얼화된 재고 관리
4. 재고 변동 이력 추적

#### 플랫폼 연동 테스트
1. 각 플랫폼별 토큰 관리
2. 상품 등록 및 동기화
3. 재고 실시간 업데이트
4. 주문 처리 및 이력 관리

#### 배송 관리 테스트
1. 배송 라벨 생성
2. 배송 상태 업데이트
3. 배치 처리 기능

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

### 환경별 설정

#### 개발 환경
- H2 인메모리 데이터베이스
- 로컬 파일 시스템 저장소
- 개발용 로그 레벨

#### 운영 환경
- MariaDB 데이터베이스
- 클라우드 스토리지 연동
- 프로덕션 로그 레벨

## 🔧 유지보수

### 로그 관리
- `backend.log`: 백엔드 서비스 로그
- `frontend.log`: 프론트엔드 빌드 로그
- 각 서비스별 상세 로그

### 데이터 백업
- H2 데이터베이스 파일 백업
- 설정 파일 백업
- 로그 파일 아카이브

### 모니터링
- 서비스 상태 체크: `BACKEND_STATUS_CHECK.md`
- API 테스트 결과: `API_TEST_REPORT.md`
- 통합 테스트 시나리오: `INTEGRATED_TEST_SCENARIOS.md`

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

**개발 상태**: 개발 완료 및 배포 준비 완료  
**마지막 업데이트**: 2025년 1월 25일  
**다음 단계**: 운영 환경 배포 및 모니터링 시스템 구축

## 📊 프로젝트 통계

- **백엔드 서비스**: 3개 (재고관리, 토큰관리, 등록서비스)
- **프론트엔드 페이지**: 21개
- **API 엔드포인트**: 30+ 개
- **플랫폼 연동**: 6개 (네이버, 쿠팡, 카페24, GPARTS, Ziparts 등)
- **테스트 시나리오**: 185개
- **문서**: 10+ 개 (가이드, 체크리스트, 테스트 시나리오)
