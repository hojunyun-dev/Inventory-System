# 📋 자동차 부품 재고 관리 시스템 - 개발 단계별 체크리스트

## 🎯 프로젝트 개요
**목적**: 다양한 온라인 마켓에 등록된 자동차 부품 재고를 한 번에 관리하는 마이크로서비스 아키텍처 기반 시스템  
**아키텍처**: Spring Boot + React + MariaDB 마이크로서비스  
**현재 상태**: 개발 진행 중 (2025년 10월)

**범례**: 
- [x] 완료 - 구현 완료
- [ ] 미완료 - 구현 전 단계

---

## ✅ 1단계: 요구사항 분석 및 기획

- [x] 비즈니스 요구사항 정의
  - [x] 다중 플랫폼 동시 관리
  - [ ] 실시간 재고 동기화
  - [x] GPARTS 카탈로그 연동으로 부품 정보 표준화
  - [x] 자동화 상품 등록 기능

- [x] 기능 명세서 작성
  - [x] 재고 관리 (입고/출고/수정/조회)
  - [x] 상품 관리 (CRUD, 이미지 관리)
  - [x] 플랫폼 통합 (토큰 관리, 자동 등록)
  - [x] 주문 관리
  - [x] 차량 호환성 관리
  - [x] 시리얼 재고 관리

- [x] 사용자 스토리 작성
  - [x] 관리자: 상품 등록/수정/삭제
  - [x] 관리자: 재고 실시간 모니터링
  - [x] 관리자: 다중 플랫폼 상품 일괄 등록
  - [x] 관리자: 플랫폼별 토큰 관리

- [x] 프로젝트 범위 확정
  - [x] 지원 플랫폼: 네이버, 쿠팡, 카페24, 번개장터, 당근마켓, 중고나라
  - [x] 아키텍처: 3개 마이크로서비스 (Backend, Token-Management, Registration)

---

## ✅ 2단계: 시스템 설계

- [x] 데이터베이스 스키마 설계 (ERD 작성)
  - [x] 상품(products), 재고(inventories), 시리얼재고(serialized_inventories)
  - [x] 주문(orders), 주문항목(order_items)
  - [x] 채널상품(channel_products), 플랫폼토큰(platform_tokens)
  - [x] 카테고리(categories), 차량호환성(vehicle_compatibilities)
  - [x] 상품이미지(product_images) - ORIGINAL/THUMBNAIL/REGISTRATION 카테고리화
  - [x] 등록템플릿(registration_templates), 등록이력(product_registrations)

- [x] API 명세서 작성
  - [x] RESTful API 설계
  - [x] `GET /api/products` - 상품 목록 조회
  - [x] `POST /api/products` - 상품 등록
  - [x] `GET /api/products/{id}` - 상품 상세 조회
  - [x] `PUT /api/products/{id}` - 상품 수정
  - [x] `DELETE /api/products/{id}` - 상품 삭제
  - [x] `GET /api/images/{productId}/registration/last/data` - 등록용 이미지 조회
  - [x] `POST /api/platforms/bunjang/register` - 번개장터 등록
  - [x] `GET /api/tokens/{platform}` - 플랫폼 토큰 조회

- [x] 시스템 아키텍처 설계
  ```
  Frontend (React:3000)
      ↓
  Backend Service (Spring Boot:8080)
      ↓           ↓
  Token Mgmt      Registration Service
  (Spring:8083)   (Spring:8082)
      ↓           ↓
      MariaDB (3307)
  ```

- [x] UI/UX 와이어프레임 작성
  - [x] 로그인 화면
  - [x] 대시보드 (통계, 최근 활동)
  - [x] 상품 목록 및 상세
  - [x] 재고 관리 (일반/시리얼)
  - [x] 플랫폼 연동 관리
  - [x] 주문 관리
  - [x] 차량 호환성 검색

---

## ✅ 3단계: 개발 환경 구축

- [x] 개발 서버 환경 설정
  - [x] Java 17 SDK 설치
  - [x] Node.js 16+ 설치
  - [x] Maven 3.6+ 설치
  - [x] IntelliJ IDEA 설정

- [x] 데이터베이스 구축
  - [x] MariaDB 10.11 Docker 컨테이너 구축 (port 3307)
  - [x] 3개 데이터베이스 생성 (inventory_system, token_management_db, registration_db)
  - [x] 테이블 스키마 자동 생성 (JPA ddl-auto: update)
  - [x] 초기 데이터 시딩 (카테고리, 샘플 상품)

- [x] 형상 관리 시스템 구축 (Git)
  - [x] Git 저장소 초기화
  - [x] GitHub 원격 저장소 연결 (https://github.com/hojunyun-dev/Inventory-System)
  - [x] .gitignore 설정 (target/, node_modules/, .env)
  - [x] feature/categories-alias 브랜치 생성 및 푸시

- [x] 개발 도구 및 라이브러리 설치
  - [x] Maven 의존성 설정 (pom.xml)
  - [x] npm 패키지 설정 (package.json)
  - [x] Docker Compose 설정
  - [x] 환경 변수 템플릿 (env.example)

---

## 🔄 4단계: 백엔드 개발

### 4.1 데이터베이스 모델 구현

- [x] 엔티티 클래스 구현 (Entity Layer)
  - [x] `Product.java` - 상품 정보 (SKU, 가격, 재고, 카탈로그 필드)
  - [x] `Inventory.java` - 재고 관리
  - [x] `SerializedInventory.java` - 시리얼 재고 관리
  - [x] `ProductImage.java` - 이미지 관리 (ImageCategory: ORIGINAL/THUMBNAIL/REGISTRATION)
  - [x] `Category.java` - 카테고리 계층 구조
  - [x] `Order.java`, `OrderItem.java` - 주문 관리
  - [x] `ChannelProduct.java` - 플랫폼별 상품 매핑
  - [x] `VehicleCompatibility.java` - 차량 호환성
  - [x] `Message.java` - 메시지 관리
  - [x] Enum: `ImageCategory.java`, `PartType.java`, `PartCondition.java`

- [x] Repository 계층 구현
  - [x] `ProductRepository.java` - JPA 쿼리 메서드
  - [x] `ProductImageRepository.java` - 이미지 카테고리별 조회 메서드
  - [x] `InventoryRepository.java`
  - [x] `SerializedInventoryRepository.java`
  - [x] `CategoryRepository.java` - 부모/자식 카테고리 조회
  - [x] `ChannelProductRepository.java`
  - [x] `OrderRepository.java`
  - [x] `VehicleCompatibilityRepository.java`
  - [x] `MessageRepository.java`

### 4.2 API 엔드포인트 개발

- [x] Controller 계층 구현
  - [x] `ProductController.java` - 상품 CRUD
  - [x] `ProductImageController.java` - 이미지 업로드/다운로드/카테고리별 조회
  - [x] `InventoryController.java` - 재고 관리
  - [x] `SerializedInventoryController.java` - 시리얼 재고
  - [x] `CategoryController.java` - 카테고리 조회
  - [x] `OrderController.java` - 주문 관리
  - [x] `ChannelProductController.java` - 채널 상품 매핑
  - [x] `VehicleCompatibilityController.java` - 차량 호환성
  - [x] `MessageController.java` - 메시지 관리
  - [x] `CatalogController.java` - GPARTS 카탈로그 연동
  - [x] `PlatformIntegrationController.java` - 플랫폼 연동
  - [x] `AutomationProxyController.java` - 자동화 프록시

### 4.3 비즈니스 로직 구현

- [x] Service 계층 구현
  - [x] `ProductService.java` - 상품 비즈니스 로직, DTO 변환, firstImageUrl 설정
  - [x] `ProductImageService.java` - 이미지 업로드/리사이징 로직
  - [x] `ImageResizeService.java` - 이미지 리사이징 (800x800 등록용, 200x200 썸네일)
  - [x] `InventoryService.java` - 재고 관리 로직
  - [x] `PlatformIntegrationService.java` - 플랫폼 API 연동 프레임워크
  - [x] `CatalogService.java` - GPARTS 카탈로그 연동 프레임워크

### 4.4 외부 플랫폼 API 연동

- [x] 플랫폼 API 연동 설정
  - [x] 네이버 커머스 API 환경 변수 설정
  - [x] 카페24 API 환경 변수 설정
  - [x] 쿠팡 API 환경 변수 설정
  - [x] GPARTS API 환경 변수 설정
  - [x] Ziparts API 환경 변수 설정
  - [ ] 네이버 커머스 API 실제 호출 로직 구현
  - [ ] 카페24 API 실제 호출 로직 구현
  - [ ] 쿠팡 API 실제 호출 로직 구현
  - [ ] GPARTS API 카탈로그 검색 구현
  - [ ] Ziparts API 카탈로그 검색 구현

- [x] 토큰 관리 서비스 (Port: 8083)
  - [x] `PlatformToken.java` - 토큰 엔티티
  - [x] `TokenRepository.java`
  - [x] `TokenService.java` - 토큰 저장/조회/갱신
  - [x] `TokenController.java` - REST API 엔드포인트

- [x] 등록 서비스 (Port: 8082) - Selenium 자동화
  - [x] `BunjangRegistrationService.java` - 번개장터 자동 등록
  - [x] `BunjangFormHandler.java` - 폼 입력 자동화 (이미지 업로드 포함)
  - [x] `BunjangLoginHandler.java` - 로그인 자동화, 토큰 캡처
  - [ ] `BunjangApiRegistrationService.java` - API 기반 등록 (TODO 남음)
  - [x] `TokenBundleService.java` - 토큰 저장 (로컬 파일 + DB)
  - [x] `BaseAutomationService.java` - 공통 자동화 로직
  - [x] `NaverSocialAutomationService.java` - 네이버 쇼핑몰 자동화
  - [x] `DanggeunAutomationService.java` - 당근마켓 자동화
  - [x] `JunggonaraAutomationService.java` - 중고나라 자동화
  - [x] `AutomationOrchestratorService.java` - 자동화 오케스트레이션

- [x] 이미지 최적화 구현
  - [x] 상품 등록 시 3가지 버전 자동 생성 (ORIGINAL/REGISTRATION/THUMBNAIL)
  - [x] REGISTRATION 이미지: 800x800px (번개장터 등록용)
  - [x] THUMBNAIL 이미지: 200x200px (목록 표시용)
  - [x] 상품 등록 시 backend에서 최신 REGISTRATION 이미지 사용

### 4.5 설정 및 보안

- [x] Configuration 설정
  - [x] `WebConfig.java` - CORS 설정, 정적 리소스 설정
  - [x] `SecurityConfig.java` - Spring Security 설정
  - [x] `DataSourceConfig.java` - 데이터소스 설정
  - [x] `DataInitializer.java` - 초기 데이터 생성

- [x] 보안 설정
  - [x] JWT 토큰 기반 인증
  - [x] 환경 변수 기반 민감정보 관리 (env.example)
  - [x] CORS 설정 (localhost:3000 허용)
  - [x] SECURITY_GUIDE.md 작성

---

## 🔄 5단계: 프론트엔드 개발

### 5.1 UI 컴포넌트 개발

- [x] 공통 컴포넌트
  - [x] `Navbar.tsx` - 상단 네비게이션
  - [x] `Sidebar.tsx` - 사이드 메뉴
  - [x] `AdvancedDashboard.tsx` - 대시보드
  - [x] `AdvancedSearch.tsx` - 고급 검색

### 5.2 화면 레이아웃 구현

- [x] 페이지 구현
  - [x] `Login.tsx` - 로그인 화면
  - [x] `Dashboard.tsx` - 대시보드 (통계, 최근 활동)
  - [x] `ProductList.tsx` - 상품 목록
  - [x] `ProductDetail.tsx` - 상품 상세
  - [x] `ProductForm.tsx` - 상품 등록/수정 폼
  - [x] `InventoryList.tsx` - 재고 목록
  - [x] `InventoryForm.tsx` - 재고 등록/수정 폼
  - [x] `SerializedInventoryList.tsx` - 시리얼 재고 목록
  - [x] `VehicleCompatibilityList.tsx` - 차량 호환성 목록
  - [x] `VehiclePartSearch.tsx` - 차량 부품 검색
  - [x] `OrderHistory.tsx` - 주문 이력
  - [x] `MessageList.tsx` - 메시지 목록
  - [x] `ShippingLabel.tsx` - 배송 라벨

- [x] 플랫폼 연동 페이지
  - [x] `ChannelProductManagement.tsx` - 채널 상품 관리 (플랫폼 선택, 등록 버튼)
  - [x] `BunjangIntegration.tsx` - 번개장터 연동
  - [x] `NaverIntegration.tsx` - 네이버 연동
  - [x] `CoupangIntegration.tsx` - 쿠팡 연동
  - [x] `Cafe24Integration.tsx` - 카페24 연동
  - [x] `GpartsIntegration.tsx` - GPARTS 연동
  - [x] `ZipartsIntegration.tsx` - Ziparts 연동

### 5.3 API 연동

- [x] API 서비스 구현
  - [x] `services/api.ts` - Axios 기반 API 클라이언트
  - [x] CRUD 메서드 (getAll, getById, create, update, delete)
  - [x] 플랫폼별 등록 메서드 (registerToBunjang 등)

- [x] Context 구현
  - [x] `contexts/AuthContext.tsx` - 인증 상태 관리

### 5.4 사용자 인터랙션 구현

- [x] 상품 이미지 업로드 기능
- [x] 이미지 미리보기 (목록에서 firstImageUrl 표시)
- [x] 플랫폼 선택 및 등록 버튼 동작
- [x] 등록 진행 상태 표시
- [x] 에러 메시지 표시

---

## 🔄 6단계: 테스트

- [ ] 단위 테스트 작성 및 실행
  - [ ] Backend Service 테스트 (ProductService, ImageService 등)
  - [ ] Frontend 컴포넌트 테스트
  - [ ] Repository 계층 테스트

- [ ] 통합 테스트 실행
  - [ ] API 엔드포인트 테스트 (Postman/curl)
  - [ ] 데이터베이스 연동 테스트
  - [ ] 마이크로서비스 간 통신 테스트

- [ ] 사용자 시나리오 테스트
  - [x] 번개장터 자동 로그인 및 상품 등록 테스트
  - [x] 토큰 저장 및 재사용 테스트
  - [ ] 당근마켓 자동 로그인 및 상품 등록 테스트
  - [ ] 중고나라 자동 로그인 및 상품 등록 테스트
  - [ ] 네이버 쇼핑몰 자동 로그인 및 상품 등록 테스트
  - [ ] 다중 플랫폼 동시 등록 테스트
  - [ ] 재고 동기화 테스트

- [ ] 버그 수정 및 재테스트
  - [x] 이미지 업로드 후 재고관리 시스템 표시 버그 수정
  - [x] 번개장터 등록 시 이미지 불일치 문제 해결
  - [x] CategoryController 404 오류 수정
  - [x] Port 8082 충돌 문제 해결

---

## 🔄 7단계: 배포 및 운영

- [x] 운영 서버 환경 구축
  - [x] Docker Compose 설정 완료
  - [x] MariaDB 컨테이너 설정
  - [x] Backend 서비스 컨테이너 설정
  - [x] Token-Management 서비스 컨테이너 설정
  - [x] Registration 서비스 컨테이너 설정
  - [x] Frontend 서비스 컨테이너 설정

- [x] 배포 스크립트 작성
  - [x] `docker-compose.yml` 작성
  - [x] 환경 변수 설정 가이드 (`ENVIRONMENT_SETUP.md`)
  - [x] `database/init/01-create-databases.sql` 작성

- [ ] 서비스 배포
  - [ ] Docker 이미지 빌드 및 푸시
  - [ ] Kubernetes 배포 (선택사항)
  - [ ] 도메인 및 SSL 설정
  - [ ] 운영 환경 변수 설정

- [ ] 모니터링 시스템 구축
  - [ ] Spring Boot Actuator 설정
  - [ ] 로그 수집 시스템 (ELK Stack 등)
  - [ ] 성능 모니터링 (Prometheus, Grafana)

---

## 🔄 8단계: 유지보수 및 개선

- [ ] 사용자 피드백 수집
  - [ ] 사용자 테스트 진행
  - [ ] 피드백 정리 및 우선순위화
  - [ ] 이슈 트래커 관리

- [ ] 버그 수정
  - [ ] 버그 리포트 수집 및 정리
  - [ ] 크리티컬 버그 우선 수정
  - [ ] 버그 재발 방지 프로세스 수립

- [ ] 기능 개선 및 추가
  - [ ] 추가 플랫폼 지원 (옥션 등)
  - [ ] 고급 검색 기능 강화
  - [ ] 대시보드 통계 개선
  - [ ] 배치 작업 스케줄링 (재고 동기화 등)
  - [ ] 알림 시스템 (이메일, SMS 등)

- [ ] 성능 최적화
  - [ ] 데이터베이스 쿼리 최적화
  - [ ] 이미지 캐싱 전략 (CDN 연동)
  - [ ] API 응답 속도 개선
  - [ ] 프론트엔드 번들 크기 최적화

---

## 📊 현재 진행 상황 요약

### 구현 완료된 기능
1. **데이터베이스 설계 및 구축** - 마이크로서비스별 3개 DB, 11개+ 엔티티
2. **백엔드 API 개발** - 14개 Controller, 주요 CRUD 기능
3. **프론트엔드 개발** - 17개 페이지 UI 구현
4. **이미지 관리 시스템** - 3가지 버전 자동 생성 (ORIGINAL/REGISTRATION/THUMBNAIL)
5. **토큰 관리 서비스** - 플랫폼 토큰 저장/조회 기능
6. **번개장터 자동 등록** - Selenium 기반 상품 등록 자동화 구현 및 테스트 완료
7. **다른 플랫폼 자동화 프레임워크** - 당근마켓, 중고나라, 네이버 쇼핑몰 자동화 구조
8. **Docker 환경 구축** - docker-compose.yml 설정
9. **Git 저장소 설정** - GitHub 연동, feature 브랜치 관리

### 테스트 완료된 기능
1. **번개장터 자동 등록** - 실제 상품 등록 동작 확인 완료
2. **토큰 저장 및 재사용** - DB 저장 및 재사용 확인 완료
3. **이미지 업로드 및 최적화** - 3가지 버전 생성 확인 완료

### 향후 구현 예정
- 다른 플랫폼 자동화 테스트 (당근마켓, 중고나라, 네이버)
- 공식 API 연동 실제 구현 (네이버, 쿠팡, 카페24)
- GPARTS/Ziparts 카탈로그 검색 기능 구현
- 플랫폼 다중 등록 (bulk-register) 기능 구현
- 단위 테스트 작성
- 배포 환경 구성
- 성능 최적화

---

## 📝 참고 문서

- [README.md](README.md) - 프로젝트 개요 및 설치 가이드
- [ENVIRONMENT_SETUP.md](ENVIRONMENT_SETUP.md) - 환경 변수 설정 가이드
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - 보안 가이드

---

**마지막 업데이트**: 2025년 10월 29일  
**체크리스트 관리자**: 프로젝트 팀
