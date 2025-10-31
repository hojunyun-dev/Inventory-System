# 📱 모바일 앱 연동 가이드

> 자동차 부품 멀티플랫폼 재고관리 시스템 API 연동 문서  
> 작성일: 2025-10-31  
> 대상: 모바일 앱 개발자

---

## 📋 목차

1. [시스템 개요](#1-시스템-개요)
2. [전체 아키텍처](#2-전체-아키텍처)
3. [주요 동작 플로우](#3-주요-동작-플로우)
4. [API 명세](#4-api-명세)
5. [앱 연동 체크리스트](#5-앱-연동-체크리스트)
6. [향후 확장 계획](#6-향후-확장-계획)

---

## 1. 시스템 개요

### 1.1 프로젝트 목적

**여러 오픈마켓(번개장터, 네이버쇼핑, 쿠팡 등)에 자동으로 상품을 등록하고, 재고를 통합 관리하는 마이크로서비스 기반 시스템**입니다.

- **재고 중앙화**: 한 번의 상품 등록으로 여러 플랫폼에 동시 배포
- **자동화 등록**: 번개장터 등 플랫폼에 자동 상품 등록
- **실시간 동기화**: 재고 변동을 실시간으로 모든 플랫폼에 반영

### 1.2 마이크로서비스 구성

| 서비스 | 포트 | 역할 |
|--------|------|------|
| **backend** | 8080 | 메인 재고/상품/주문 관리 API (앱은 주로 여기 연동) |
| **token-service** | 8083 | 플랫폼별 로그인 토큰·쿠키 관리 |
| **registration-service** | 8082 | Selenium 기반 브라우저 자동화 + 상품 등록 |
| **frontend** | 3000 | 현재 테스트용 웹 UI (앱으로 대체 예정) |
| **mariadb** | 3307 | 공통 데이터 저장소 |
| **selenium** | 4444, 7900 | 브라우저 자동화 컨테이너 (noVNC 시각화) |

### 1.3 기술 스택

```
Backend:  Spring Boot 3.2.0, Java 17, MariaDB
자동화:   Selenium WebDriver, Selenium Grid, noVNC
컨테이너: Docker Compose
통신:     REST API, WebClient (Spring)
```

---

## 2. 전체 아키텍처

### 2.1 서비스 구조 다이어그램

```
                    ┌─────────────────────┐
                    │   모바일 앱 (나중에) │
                    └──────────┬──────────┘
                               │
                    ┌──────────▼──────────┐
                    │                     │
                    │  Backend Service    │
                    │     Port: 8080      │
                    │                     │
                    │  • 상품 CRUD        │
                    │  • 재고 관리        │
                    │  • 주문 관리        │
                    │  • 이미지 관리      │
                    └───────┬──────────┬──┘
                            │          │
                            │          │ WebClient
                            │          │
                ┌───────────▼──┐  ┌───▼───────────────┐
                │              │  │                   │
                │ Token        │  │  Registration     │
                │ Service      │  │  Service          │
                │ Port: 8083   │  │  Port: 8082       │
                │              │  │                   │
                │ • 토큰 저장  │  │ • 브라우저 자동화 │
                │ • 토큰 조회  │  │ • 상품 등록       │
                │ • 토큰 삭제  │  │ • 로그인 세션 관리│
                └──────────────┘  └────────┬──────────┘
                                           │
                              ┌────────────▼────────────┐
                              │                         │
                              │  Selenium Grid          │
                              │  Port: 4444 (Hub)       │
                              │  Port: 7900 (noVNC)     │
                              │                         │
                              │  • Chrome 브라우저      │
                              │  • 원격 제어            │
                              └─────────────────────────┘
```

### 2.2 Docker Compose 통합 운영

```yaml
# docker-compose.yml
services:
  mariadb:        # 데이터베이스
  backend:        # 메인 API 서버
  token-service:  # 토큰 관리
  registration-service:  # 자동화 서비스
  selenium:       # 브라우저 자동화
  frontend:       # 현재 웹 UI (앱으로 교체)
```

**핵심 포인트**:
- 앱은 **Backend Service (8080)** 에만 직접 연동
- Backend가 필요 시 내부적으로 다른 서비스 호출
- Docker 네트워크 내부에서만 서비스 간 통신

---

## 3. 주요 동작 플로우

### 3.1 상품 등록 플로우 (번개장터 예시)

```
[앱] "번개장터 등록" 버튼 클릭
    ↓
[Backend 8080] POST /api/products/register
    {
      productId: 123,
      platform: "BUNJANG",
      quantity: 5
    }
    ↓
[Backend] 로그인 상태 확인
    ↓
[반환 A - 로그인됨]
    → registration-service의 API 기반 등록
    → 즉시 상품 등록 완료
    ↓
[앱] "등록 완료" 메시지 표시

[반환 B - 미로그인]
    → 등록 서비스 브라우저 창 열림 (noVNC)
    → 사용자 수동 로그인
    → 로그인 완료 시 토큰 저장
    → 자동으로 상품 등록 진행
    ↓
[앱] "등록 완료" 메시지 표시
```

### 3.2 첫 로그인 플로우 (세션 생성)

```
[앱] "플랫폼 연결" 버튼 클릭
    ↓
[Backend] GET /api/automation/bunjang/session/status
    → "loggedIn": false
    ↓
[앱] noVNC 브라우저 창 자동 열림 (localhost:7900)
    ↓
[사용자] 번개장터에서 네이버 로그인 수동 진행
    ↓
[registration-service]
    → JavaScript 후킹으로 로그인 감지
    → x-bun-auth-token 추출
    → bun_session 쿠키 추출
    ↓
[token-service] 토큰 저장
    ↓
[registration-service] API 기반 상품 등록 시작
    ↓
[앱] "로그인 완료, 상품 등록 진행 중..." 표시
```

### 3.3 세션 재사용 로직

```
[앱] 두 번째 상품 등록 시도
    ↓
[Backend] GET /api/automation/bunjang/session/status
    → 브라우저 세션 확인
    → 토큰 유효성 확인
    ↓
[시나리오 A - 세션 유지됨]
    → registration-service의 기존 브라우저 재사용
    → 로그인 상태 확인 (6개 UI 요소 중 2개 이상 존재)
    → API 기반 등록 즉시 진행
    ↓
[시나리오 B - 로그아웃 감지됨]
    → 기존 브라우저 세션 종료
    → DB 토큰 삭제
    → 새 브라우저 세션 생성
    → 첫 로그인 플로우로 전환
```

---

## 4. API 명세

### 4.1 인증 API

#### 로그인
```http
POST /auth/login
Content-Type: application/json

Request Body:
{
  "username": "admin",
  "password": "admin123"
}

Response 200 OK:
{
  "message": "로그인 성공",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "timestamp": "2025-10-31T05:20:00Z"
}
```

### 4.2 상품 관리 API

#### 상품 목록 조회
```http
GET /api/products
Authorization: Bearer {JWT_TOKEN}

Response 200 OK:
[
  {
    "id": 1,
    "sku": "ENG-OIL-001",
    "name": "현대 소나타 점화플러그 (이리듐)",
    "price": 95000.0,
    "quantity": 30,
    "firstImageUrl": "/api/images/1/registration/last/data",
    "imageCount": 3,
    "categoryId": 1,
    "categoryName": "엔진오일"
  }
]
```

#### 상품 상세 조회
```http
GET /api/products/{productId}
Authorization: Bearer {JWT_TOKEN}

Response 200 OK:
{
  "id": 1,
  "sku": "ENG-OIL-001",
  "name": "현대 소나타 점화플러그 (이리듐)",
  "description": "현대 소나타 전용 이리듐 점화플러그",
  "price": 95000.0,
  "quantity": 30,
  "images": [
    {
      "id": 1,
      "imageUrl": "/api/images/1/data",
      "category": "ORIGINAL"
    }
  ]
}
```

#### 이미지 업로드
```http
POST /api/images/{productId}
Authorization: Bearer {JWT_TOKEN}
Content-Type: multipart/form-data

Form Data:
- file: [이미지 파일]

Response 200 OK:
{
  "message": "이미지 업로드 완료",
  "imageId": 123,
  "imageCategory": "ORIGINAL",
  "originalImageId": 123
}
```

**참고**: 한 번의 업로드로 3가지 버전 자동 생성
- `ORIGINAL`: 원본 이미지
- `REGISTRATION`: 800x800px 등록용
- `THUMBNAIL`: 200x200px 썸네일

### 4.3 플랫폼 등록 API

#### 번개장터 상품 등록
```http
POST /api/automation/platform/bunjang/register
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json

Request Body:
{
  "productId": 123,
  "productName": "현대 소나타 점화플러그 (이리듐)",
  "description": "현대 소나타 전용 이리듐 점화플러그",
  "price": 95000,
  "quantity": 5,
  "category": "기타"
}

Response 200 OK (로그인 상태):
{
  "success": true,
  "message": "기존 세션으로 상품등록 완료",
  "apiResult": {
    "data": {
      "pid": "366190702"
    }
  }
}

Response 200 OK (미로그인 상태):
{
  "success": true,
  "message": "브라우저 창이 열렸습니다. 로그인 후 상품이 자동 등록됩니다."
}
```

#### 로그인 상태 확인
```http
GET /api/automation/bunjang/session/status
Authorization: Bearer {JWT_TOKEN}

Response 200 OK (로그인됨):
{
  "success": true,
  "loggedIn": true,
  "message": "토큰 및 브라우저 세션 모두 유효"
}

Response 200 OK (미로그인):
{
  "success": true,
  "loggedIn": false,
  "message": "브라우저 세션이 없습니다. 로그인이 필요합니다."
}
```

### 4.4 공통 응답 형식

모든 API 응답은 다음 형식을 따릅니다:

```json
{
  "message": "작업 완료 메시지",
  "data": { ... },
  "timestamp": "2025-10-31T05:20:00Z"
}
```

**에러 응답 (400/500)**:
```json
{
  "message": "에러 메시지",
  "error": "에러 상세",
  "timestamp": "2025-10-31T05:20:00Z"
}
```

---

## 5. 앱 연동 체크리스트

### 5.1 서버 주소 설정

```typescript
// 앱 환경 설정
const API_CONFIG = {
  baseURL: 'http://localhost:8080',  // 개발용
  // baseURL: 'https://api.inventory-system.com',  // 운영용
  
  endpoints: {
    auth: '/auth/login',
    products: '/api/products',
    register: '/api/automation/platform/bunjang/register',
    sessionStatus: '/api/automation/bunjang/session/status'
  }
};
```

### 5.2 인증 처리

#### JWT 토큰 저장
```typescript
// 로그인 후
const response = await axios.post('/auth/login', { username, password });
const token = response.data.token;

// 앱 로컬 저장소에 저장
await AsyncStorage.setItem('authToken', token);
```

#### API 요청 시 토큰 포함
```typescript
const axiosInstance = axios.create({
  baseURL: API_CONFIG.baseURL,
  headers: {
    'Authorization': `Bearer ${await AsyncStorage.getItem('authToken')}`
  }
});

// 사용 예시
const products = await axiosInstance.get('/api/products');
```

#### 토큰 만료 처리
```typescript
// 401/403 응답 시 로그인 페이지로 리다이렉트
axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      // 로그인 페이지로 이동
      NavigationService.navigate('Login');
    }
    return Promise.reject(error);
  }
);
```

### 5.3 이미지 처리

#### 이미지 업로드 예시
```typescript
const uploadImage = async (productId: number, imageUri: string) => {
  const formData = new FormData();
  formData.append('file', {
    uri: imageUri,
    type: 'image/jpeg',
    name: 'product.jpg'
  });

  const response = await axiosInstance.post(
    `/api/images/${productId}`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  );

  return response.data;
};
```

#### 이미지 표시
```typescript
// 상품 목록에서 이미지 URL 조합
const imageUrl = `${API_CONFIG.baseURL}${product.firstImageUrl}`;
// 예: http://localhost:8080/api/images/1/registration/last/data

<Image 
  source={{ uri: imageUrl }}
  style={{ width: 200, height: 200 }}
/>
```

### 5.4 상품 등록 플로우 구현

```typescript
const registerToBunjang = async (product: Product) => {
  try {
    // 1. 로그인 상태 확인
    const statusResponse = await axiosInstance.get('/api/automation/bunjang/session/status');
    
    if (!statusResponse.data.loggedIn) {
      // 미로그인: 브라우저 창 열기 안내
      Alert.alert(
        '로그인 필요',
        '번개장터에 로그인이 필요합니다. 브라우저에서 로그인해주세요.',
        [{ text: '확인' }]
      );
      
      // 등록 서비스에 등록 요청 (브라우저 자동 열림)
      await axiosInstance.post('/api/automation/platform/bunjang/register', {
        productId: product.id,
        productName: product.name,
        description: product.description,
        price: product.price,
        quantity: product.quantity,
        category: product.category
      });
      
      return;
    }

    // 2. 로그인됨: 즉시 등록
    const response = await axiosInstance.post('/api/automation/platform/bunjang/register', {
      productId: product.id,
      productName: product.name,
      description: product.description,
      price: product.price,
      quantity: product.quantity,
      category: product.category
    });

    if (response.data.success) {
      Alert.alert('성공', '번개장터에 상품이 등록되었습니다!');
    }
    
  } catch (error) {
    Alert.alert('오류', '상품 등록 중 오류가 발생했습니다.');
    console.error(error);
  }
};
```

### 5.5 CORS 및 보안 설정

**앱 개발 시 확인사항**:
- 백엔드 `SecurityConfig.java`에서 앱 도메인 허용 필요
- 현재: `localhost:3000` (웹 프론트엔드) 허용
- **추가 필요**: 앱 도메인 (`capacitor://localhost`, `http://localhost` 등)

### 5.6 테스트 시나리오

#### 시나리오 1: 정상 등록
```
1. 앱 실행 → 로그인
2. 상품 목록 조회 성공 확인
3. 상품 선택 → "번개장터 등록" 클릭
4. 로그인 상태 확인 → 로그인됨
5. 즉시 등록 → 성공 메시지
```

#### 시나리오 2: 첫 로그인
```
1. 앱 실행 → 로그인
2. 상품 선택 → "번개장터 등록" 클릭
3. 로그인 상태 확인 → 미로그인
4. 사용자에게 "브라우저에서 로그인 필요" 안내
5. 등록 서비스가 브라우저 창 열어줌 (noVNC)
6. 사용자 수동 로그인
7. 자동으로 상품 등록 진행 → 성공
```

#### 시나리오 3: 세션 만료 후 재등록
```
1. 첫 로그인 완료 → 상품 등록 성공
2. 브라우저에서 로그아웃
3. 앱에서 다른 상품 등록 시도
4. 시스템이 로그아웃 자동 감지
5. 새 브라우저 세션 생성 → 첫 로그인 플로우
6. 상품 등록 성공
```

---

## 6. 향후 확장 계획

### 6.1 현재 구현 상태

✅ **완료**:
- 번개장터 단일 플랫폼 자동 등록
- Docker Compose 통합 운영
- Selenium Grid + noVNC 브라우저 제어
- 토큰 기반 API 등록
- 브라우저 세션 자동 재사용
- 로그인/로그아웃 자동 감지

### 6.2 다음 단계

🔄 **개발 중**:
- 여러 플랫폼 병렬 등록 (네이버, 당근마켓 등)
- 로그인 세션 영구 저장 및 자동 갱신
- 앱에서 플랫폼 연결/해제 UI

### 6.3 최종 목표

**"한 번의 로그인, 여러 플랫폼 자동 등록"**

```
[사용자 작업]
1. 처음 한 번만 각 플랫폼에 로그인
   → 토큰이 자동 저장됨

2. 이후 앱에서 상품 등록
   → 시스템이 자동으로:
     - 로그인 상태 확인
     - 토큰 유효성 검증
     - 여러 플랫폼에 동시 등록

[결과]
사용자는 로그인 없이 바로 등록 가능
```

---

## 📞 문의

- 기술 문의: 프로젝트 이슈 생성
- 문서 개선: Pull Request 환영
- 버그 리포트: GitHub Issues

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-10-31  
**작성자**: Inventory System Team

