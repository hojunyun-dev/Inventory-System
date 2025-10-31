# 🚗 자동차 부품 재고 관리 시스템 - 프로젝트 개요서

## 📋 1. 프로젝트 소개

### 1.1 시스템 목적
다양한 온라인 마켓플레이스(번개장터, 네이버쇼핑, 쿠팡 등)에 등록된 자동차 부품 재고를 **한 곳에서 통합 관리**하는 마이크로서비스 아키텍처 기반 시스템입니다.

### 1.2 핵심 비즈니스 가치
- **재고 중앙화 관리**: 여러 판매 채널의 부품 정보 통합 관리
- **자동화 상품 등록**: Selenium 기반 웹 자동화로 상품 일괄 등록
- **이미지 최적화**: 플랫폼별 최적화된 이미지 자동 생성 (원본/썸네일/등록용)
- **실시간 동기화**: 재고 변동 실시간 모니터링 및 반영
- **카탈로그 표준화**: GPARTS 연동을 통한 부품 정보 표준화

### 1.3 주요 기술 스택
| 계층 | 기술 |
|------|------|
| Backend | Spring Boot 3.2.0, Java 17 |
| Frontend | React 18.2.0, TypeScript, Material-UI |
| Database | MariaDB 10.11 |
| Automation | Selenium WebDriver, ChromeDriver, Selenium Grid |
| Communication | REST API, WebClient (Spring) |
| Container | Docker, Docker Compose |
| Remote Control | noVNC (VNC 웹 클라이언트) |

---

## 🏗️ 2. 시스템 아키텍처

### 2.1 마이크로서비스 구조

```
┌─────────────────────────────────────────────────────────┐
│              Frontend (React)                           │
│              Port: 3000                                 │
│  - 상품 관리 UI                                          │
│  - 플랫폼 등록 화면                                      │
│  - 대시보드                                             │
└─────────────────┬───────────────────────────────────────┘
                  │ REST API (HTTP)
┌─────────────────▼───────────────────────────────────────┐
│          Backend Service (Spring Boot)                  │
│          Port: 8080                                     │
│          inventory_system DB                            │
│  ┌──────────────────────────────────────────┐          │
│  │ • ProductController                      │          │
│  │ • ProductImageController                 │          │
│  │ • InventoryController                    │          │
│  │ • CategoryController                     │          │
│  │ • PlatformIntegrationController          │          │
│  └──────────────────────────────────────────┘          │
└──────┬────────────────────┬────────────────────────────┘
       │                    │
       │ WebClient          │ WebClient
┌──────▼───────────┐  ┌────▼───────────────────────────┐
│ Token Management │  │ Registration Service           │
│     Service      │  │ Port: 8082                    │
│   Port: 8083     │  │ registration_db               │
│ token_management │  └───────────────────────────────┘
│       _db        │  ┌───────────────────────────────┐
└──────────────────┘  │ • BunjangRegistrationService  │
                      │ • BunjangApiRegistrationService│
                      │ • BunjangLoginHandler         │
                      │ • TokenBundleService          │
                      │ • BaseAutomationService       │
                      └───────────────────────────────┘
                              │
                              ▼
                  ┌───────────────────────────┐
                  │   Selenium Grid           │
                  │   Port: 4444              │
                  │   noVNC: Port: 7900       │
                  │   (Remote Browser Control)│
                  └───────────────────────────┘
```

### 2.2 서비스 역할 분리

#### Backend Service (8080)
- **역할**: 메인 재고 관리 시스템
- **데이터베이스**: `inventory_system`
- **주요 기능**:
  - 상품 CRUD
  - 이미지 업로드/리사이징
  - 재고 관리
  - 카테고리 관리
  - 주문 관리

#### Token Management Service (8083)
- **역할**: 플랫폼별 인증 토큰 관리
- **데이터베이스**: `token_management_db`
- **주요 기능**:
  - 토큰 저장/조회/갱신
  - 토큰 만료 관리
  - 플랫폼별 토큰 분리 관리

#### Registration Service (8082)
- **역할**: 플랫폼별 상품 자동 등록
- **데이터베이스**: `registration_db`
- **주요 기능**:
  - Selenium Grid 기반 웹 자동화 (RemoteWebDriver)
  - 번개장터 상품 등록 (구현 완료)
  - API 기반 상품 등록 (토큰 기반)
  - 브라우저 세션 관리 및 자동 재로그인
  - 로그아웃 감지 및 토큰 자동 삭제
  - 당근마켓/중고나라/네이버 자동화 (구조만 존재)
  
#### Selenium Grid
- **역할**: 원격 브라우저 자동화 서비스
- **포트**: 4444 (WebDriver), 7900 (noVNC)
- **주요 기능**:
  - Chrome 브라우저 원격 제어
  - noVNC를 통한 웹 기반 브라우저 시각화
  - Docker 컨테이너 기반 격리된 환경 제공

---

## 💾 3. 데이터베이스 설계

### 3.1 데이터베이스별 역할

| 데이터베이스명 | 역할 | 주요 테이블 |
|--------------|------|------------|
| `inventory_system` | 재고 관리 | products, inventories, product_images, categories, orders |
| `token_management_db` | 토큰 관리 | platform_tokens, platform_accounts |
| `registration_db` | 등록 이력 | product_registrations, registration_templates |

### 3.2 핵심 엔티티 및 연관관계

#### 3.2.1 Product (상품)
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String sku;  // 재고 관리 단위
    
    @Column(nullable = false)
    private String name;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity = 0;
    
    // 카탈로그 표준화 필드
    @Enumerated(EnumType.STRING)
    @Column(name = "part_type")
    private PartType partType;  // OEM, AFTERMARKET
    
    @Enumerated(EnumType.STRING)
    @Column(name = "part_condition")
    private PartCondition partCondition;  // NEW, USED
    
    @Column(name = "oem_part_number")
    private String oemPartNumber;
    
    @Column(name = "manufacturer_name")
    private String manufacturerName;
    
    // 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images = new ArrayList<>();
}
```

**핵심 필드 설명**:
- `sku`: 재고 관리 단위 (고유값)
- `partType`, `partCondition`: GPARTS 카탈로그 호환성
- `images`: 상품과 일대다 관계 (ProductImage)

#### 3.2.2 ProductImage (상품 이미지)
```java
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;  // 상품과 다대일 관계
    
    @Column(name = "image_name", nullable = false)
    private String imageName;
    
    @Lob
    @Column(name = "image_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imageData;  // 실제 이미지 바이너리 데이터
    
    @Column(name = "image_type", nullable = false)
    private String imageType;  // MIME 타입
    
    @Column(name = "image_size", nullable = false)
    private Long imageSize;
    
    /**
     * 이미지 카테고리
     * - ORIGINAL: 원본 이미지
     * - THUMBNAIL: 썸네일 (200x200px)
     * - REGISTRATION: 등록용 (800x800px)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "image_category", nullable = false)
    private ImageCategory imageCategory;
    
    @Column(name = "original_image_id")
    private Long originalImageId;  // 리사이징된 이미지가 원본을 참조
}
```

**핵심 설계**:
- LONGBLOB로 실제 이미지 저장 (외부 파일시스템 불필요)
- `image_category`로 용도별 이미지 구분
- `originalImageId`로 원본-파생 이미지 연결

#### 3.2.3 Inventory (재고)
```java
@Entity
@Table(name = "inventories")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity;  // 재고 수량
    
    @Column(name = "warehouse_location")
    private String warehouseLocation;  // 창고 위치
}
```

#### 3.2.4 ChannelProduct (채널 상품)
```java
@Entity
@Table(name = "channel_products")
public class ChannelProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private String platform;  // BUNJANG, NAVER, COUPANG 등
    
    @Column(name = "external_product_id")
    private String externalProductId;  // 플랫폼 상품 ID
}
```

#### 3.2.5 TokenBundle (토큰 번들)
```java
// 공통 DTO - registration_db의 platform_tokens 테이블과 매핑
public class TokenBundle {
    public String platform;            // "BUNJANG"
    public List<CookieEntry> cookies;  // 쿠키 리스트 (name=value)
    public String csrf;                // CSRF 토큰 (nullable)
    public String authToken;           // x-bun-auth-token
    public Instant expiresAt;          // 만료 시간 (nullable)
}

public class CookieEntry {
    public String name;
    public String value;
    public String domain;
    public String path;
    public Long expiryEpochSec;
    public Boolean httpOnly;
    public Boolean secure;
}
```

**핵심 설계**:
- `TokenBundle`: 여러 토큰을 묶어서 관리
- `authToken`: API 인증에 사용되는 32자리 hex 토큰
- `cookies`: 브라우저 세션 쿠키들

### 3.3 데이터베이스 관계도 (ERD)

```
Category (1) ──< (N) Product (1) ──< (N) ProductImage
                │
                │ (1) ──< (N) Inventory
                │
                │ (1) ──< (N) ChannelProduct
                │
                │ (1) ──< (N) OrderItem ──> (N) Order
```

---

## 🔄 4. 핵심 비즈니스 로직

### 4.1 상품 이미지 업로드 및 최적화 플로우

```
사용자가 이미지 업로드
        ↓
ProductImageService.uploadImage()
        ↓
1. 파일 유효성 검사 (크기, 타입)
        ↓
2. 원본 이미지 저장 (ORIGINAL)
        ↓
3. ImageResizeService.resizeForRegistration()
   → 800x800px 등록용 이미지 생성 (REGISTRATION)
        ↓
4. ImageResizeService.createThumbnail()
   → 200x200px 썸네일 생성 (THUMBNAIL)
        ↓
5. DB에 3가지 버전 모두 저장
   - ORIGINAL: 원본 (예: 2MB)
   - REGISTRATION: 800x800 (예: 200KB)
   - THUMBNAIL: 200x200 (예: 50KB)
```

**핵심 코드 (ProductImageService.java)**:
```java
public ProductImage uploadImage(Long productId, MultipartFile file) {
    // 1. 상품 존재 확인
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));
    
    // 2. 파일 유효성 검사
    validateImageFile(file);
    
    byte[] originalImageData = file.getBytes();
    
    // 3. 원본 저장
    ProductImage originalImage = createImageEntity(product, fileName, 
        originalImageData, contentType, ImageCategory.ORIGINAL, null);
    ProductImage savedOriginal = productImageRepository.save(originalImage);
    
    // 4. 등록용 이미지 생성 및 저장
    byte[] registrationImageData = imageResizeService.resizeForRegistration(
        originalImageData, contentType);
    ProductImage registrationImage = createImageEntity(product, fileName, 
        registrationImageData, contentType, ImageCategory.REGISTRATION, 
        savedOriginal.getId());
    productImageRepository.save(registrationImage);
    
    // 5. 썸네일 생성 및 저장
    byte[] thumbnailImageData = imageResizeService.createThumbnail(
        originalImageData, contentType);
    ProductImage thumbnailImage = createImageEntity(product, fileName, 
        thumbnailImageData, contentType, ImageCategory.THUMBNAIL, 
        savedOriginal.getId());
    productImageRepository.save(thumbnailImage);
    
    return savedOriginal;
}
```

### 4.2 번개장터 상품 등록 자동화 플로우

```
프론트엔드에서 "번개장터 등록" 버튼 클릭
        ↓
1. 로그인 상태 확인 (GET /api/automation/bunjang/session/status)
   → 브라우저 세션 존재 시 실제 상태 확인
   → 토큰 존재 시 유효성 확인
        ↓
2-A. 로그인된 경우 (토큰 유효)
   → BunjangApiRegistrationService.registerProduct()
   → API 기반 상품 등록 수행
        ↓
2-B. 로그인되지 않은 경우
   → noVNC 브라우저 창 자동 열기 (localhost:7900)
   → BunjangRegistrationService.openForManualLogin()
        ↓
   2-B-1. WebDriver 생성 (Selenium Grid 연결)
        ↓
   2-B-2. 브라우저 상태 확인
      → 로그아웃 감지 시 기존 세션 종료
      → 새 세션 생성
        ↓
   2-B-3. 수동 로그인 플로우
      → 홈페이지 이동
      → 로그인 버튼 클릭
      → 네이버 로그인 버튼 클릭
      → 60초 대기 (사용자 수동 로그인)
        ↓
   2-B-4. 로그인 완료 감지
      → 로그인 요소 확인 (6개 중 2개 이상)
        ↓
   2-B-5. 토큰 캡처 및 저장
      → x-bun-auth-token 추출
      → TokenBundleService.saveTokenBundle()
      → API 기반 상품 등록 수행
        ↓
3. 상품 등록 완료
   → 상품 URL 반환
```

**핵심 코드 (BunjangRegistrationService.java)**:
```java
// 로그인 상태 확인 및 세션 관리
public Map<String, Object> checkLoginStatusLight() {
    // 1. 토큰 기반 확인
    TokenBundle tb = tokenBundleService.getTokenBundle("BUNJANG");
    boolean hasToken = (tb != null) && !tokenBundleService.isExpired(tb);
    
    // 2. 브라우저 실제 상태 확인
    if (webDriver != null) {
        webDriver.get("https://m.bunjang.co.kr/");
        // 로그아웃 플래그 확인
        checkAndHandleLogoutFlag(webDriver);
        boolean isLoggedIn = loginHandler.isLoggedIn(webDriver);
        
        // 로그아웃 감지 시 토큰 삭제
        if (!isLoggedIn && hasToken) {
            tokenBundleService.deleteTokenBundle("BUNJANG");
        }
        
        return Map.of("loggedIn", isLoggedIn, ...);
    }
    
    return Map.of("loggedIn", false, ...);
}
```

### 4.3 상품 목록 조회 플로우

```
GET /api/products
        ↓
ProductService.getAllProducts()
        ↓
1. ProductRepository.findAll()
   → DB에서 모든 상품 조회
        ↓
2. convertToDto() - 각 상품을 DTO로 변환
        ↓
3. ProductImageRepository.countByProductId()
   → 이미지 개수 조회
        ↓
4. firstImageUrl 설정
   → "/api/images/{productId}/registration/last/data"
   → 최신 REGISTRATION 이미지 URL
        ↓
5. 프론트엔드에 반환
   → ProductDto[] 형태로 JSON 응답
```

**핵심 코드 (ProductService.java)**:
```java
private ProductDto convertToDto(Product product) {
    ProductDto dto = new ProductDto();
    dto.setId(product.getId());
    dto.setSku(product.getSku());
    dto.setName(product.getName());
    dto.setPrice(product.getPrice());
    dto.setQuantity(product.getQuantity());
    
    // 이미지 정보 추가
    long imageCount = productImageRepository.countByProductId(product.getId());
    dto.setImageCount((int) imageCount);
    
    if (imageCount > 0) {
        // 최신 등록용 이미지를 대표 이미지로 설정
        dto.setFirstImageUrl("/api/images/" + product.getId() 
            + "/registration/last/data");
    }
    
    return dto;
}
```

---

## 🎨 5. 프론트엔드 구조

### 5.1 주요 페이지

| 페이지 | 파일 | 설명 |
|--------|------|------|
| 상품 목록 | `ProductList.tsx` | 재고 관리 시스템의 상품 목록 |
| 상품 상세 | `ProductDetail.tsx` | 상품 정보 및 이미지 표시 |
| 상품 등록 | `ProductForm.tsx` | 신규 상품 및 이미지 업로드 |
| 채널 상품 관리 | `ChannelProductManagement.tsx` | 플랫폼별 등록 선택 화면 |
| 번개장터 연동 | `BunjangIntegration.tsx` | 번개장터 등록 전용 화면 |
| 대시보드 | `Dashboard.tsx` | 통계 및 최근 활동 |

### 5.2 API 서비스 레이어

**frontend/src/services/api.ts**:
```typescript
const API_BASE_URL = 'http://localhost:8080/api';

export const api = {
  // 상품 목록 조회
  getAll: () => axios.get(`${API_BASE_URL}/products`),
  
  // 상품 등록
  create: (data: ProductDto) => axios.post(`${API_BASE_URL}/products`, data),
  
  // 번개장터 등록
  registerToBunjang: (productId: number) => 
    axios.post(`${API_BASE_URL}/automation/bunjang/register`, { productId }),
};
```

### 5.3 이미지 표시 로직

```typescript
// ProductList.tsx
{products.map(product => (
  <Card key={product.id}>
    <CardMedia
      component="img"
      height="200"
      image={product.firstImageUrl}  // "/api/images/2/registration/last/data"
      alt={product.name}
    />
    <CardContent>
      <Typography>{product.name}</Typography>
      <Typography>가격: {product.price}원</Typography>
      <Typography>재고: {product.quantity}개</Typography>
    </CardContent>
  </Card>
))}
```

---

## 🔗 6. 마이크로서비스 간 통신

### 6.1 Backend → Registration Service

**목적**: 상품 정보 및 이미지 전달

```java
// Backend에서 상품 정보 조회
String productApi = "http://localhost:8080/api/products/" + productId;
Map<String, Object> productDto = webClient.get()
    .uri(productApi)
    .retrieve()
    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
    .block();

// firstImageUrl에서 이미지 데이터 다운로드
String imageUrl = String.valueOf(productDto.get("firstImageUrl"));
byte[] imageData = webClient.get()
    .uri(imageUrl.startsWith("http") ? imageUrl : ("http://localhost:8080" + imageUrl))
    .retrieve()
    .bodyToMono(byte[].class)
    .block();
```

### 6.2 Registration Service → Token Management Service

**목적**: 플랫폼 토큰 저장

```java
// TokenBundleService.java
private void saveToTokenManagementService(TokenBundle bundle) {
    String url = tokenManagementServiceUrl + "/api/tokens";
    
    Map<String, Object> tokenData = new HashMap<>();
    tokenData.put("platform", bundle.getPlatform());
    tokenData.put("accessToken", bundle.getAccessToken());
    tokenData.put("refreshToken", bundle.getRefreshToken());
    tokenData.put("expiresAt", bundle.getExpiresAt());
    
    restTemplate.postForObject(url, tokenData, Map.class);
}
```

---

## 📊 7. 주요 API 엔드포인트

### 7.1 상품 관리 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/products` | 상품 목록 조회 |
| POST | `/api/products` | 상품 등록 |
| GET | `/api/products/{id}` | 상품 상세 조회 |
| PUT | `/api/products/{id}` | 상품 수정 |
| DELETE | `/api/products/{id}` | 상품 삭제 |

### 7.2 이미지 관리 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/images/{productId}` | 이미지 업로드 (3가지 버전 자동 생성) |
| GET | `/api/images/{productId}/registration/last/data` | 최신 등록용 이미지 조회 |
| GET | `/api/images/{imageId}/data` | 특정 이미지 데이터 조회 |
| DELETE | `/api/images/{imageId}` | 이미지 삭제 |

### 7.3 자동화 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/automation/platform/bunjang/register` | 번개장터 상품 등록 (로그인 상태 자동 확인) |
| GET | `/api/automation/bunjang/session/status` | 브라우저 세션 상태 확인 |
| POST | `/api/automation/platform/bunjang/session/open-with-product` | 상품 정보와 함께 로그인 브라우저 열기 |

### 7.4 응답 예시

**GET /api/products 응답**:
```json
[
  {
    "id": 2,
    "sku": "ENG-OIL-002",
    "name": "기아 5W-40 합성엔진오일",
    "price": 28000.00,
    "quantity": 30,
    "firstImageUrl": "/api/images/2/registration/last/data",
    "imageCount": 3,
    "categoryId": 1,
    "categoryName": "엔진오일"
  }
]
```

---

## 🚀 8. 실행 및 배포

### 8.1 로컬 개발 환경 실행

```bash
# 1. MariaDB 시작 (Docker)
docker-compose up -d mariadb

# 2. Backend 실행
cd backend
mvn spring-boot:run

# 3. Token Management Service 실행
cd ../token-management-service
mvn spring-boot:run

# 4. Registration Service 실행
cd ../registration-service
mvn spring-boot:run

# 5. Frontend 실행
cd ../frontend
npm install
npm start
```

### 8.2 Docker Compose로 전체 실행

```bash
# 전체 서비스 빌드 및 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f backend
docker-compose logs -f registration-service
docker-compose logs -f selenium

# 특정 서비스 재빌드
docker-compose build registration-service
docker-compose up -d registration-service
```

**주요 서비스 포트**:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Registration Service: http://localhost:8082
- Token Service: http://localhost:8083
- Selenium Grid: http://localhost:4444
- noVNC (브라우저 시각화): http://localhost:7900

---

## 📝 9. 프로젝트 현재 상태

### 9.1 구현 완료된 기능 ✅
- 상품 CRUD 및 이미지 업로드/리사이징
- Docker Compose 기반 전체 시스템 통합 운영
- Selenium Grid + noVNC 원격 브라우저 제어
- 번개장터 API 기반 상품 등록 (토큰 인증)
- 번개장터 자동화 상품 등록 (Selenium)
- 토큰 캡처 및 저장 (DB)
- 3가지 버전 이미지 자동 생성 (ORIGINAL/REGISTRATION/THUMBNAIL)
- 브라우저 세션 관리 및 자동 재사용
- 로그인/로그아웃 상태 자동 감지
- 마이크로서비스 아키텍처 구축
- Frontend-Backend 연동
- CORS 설정 및 Spring Security 통합

### 9.2 향후 구현 예정 🔄
- 다른 플랫폼 자동화 테스트 (당근마켓, 중고나라, 네이버)
- 공식 API 연동 (네이버, 쿠팡, 카페24)
- GPARTS/Ziparts 카탈로그 검색
- 단위 테스트 작성
- 배포 환경 구성

---

## 📞 10. 참고 문서
- [PROJECT_CHECKLIST.md](PROJECT_CHECKLIST.md) - 개발 단계별 체크리스트
- [README.md](README.md) - 프로젝트 기본 문서
- [ENVIRONMENT_SETUP.md](ENVIRONMENT_SETUP.md) - 환경 설정 가이드
- [SECURITY_GUIDE.md](SECURITY_GUIDE.md) - 보안 가이드

---

## 🔐 11. 세션 관리 및 자동화

### 11.1 브라우저 세션 관리

**세션 유지 정책**:
- 브라우저 세션은 `registration-service`가 살아있는 동안 유지됨 (무기한)
- 수동 재시작/종료 시에만 세션 종료
- Selenium Grid의 `SE_NODE_MAX_SESSIONS=1` 설정으로 동시 1개 세션만 지원

**세션 재사용 로직**:
```java
// BunjangRegistrationService.java - ensureDriver()
if (webDriver != null) {
    // 브라우저 실제 로그인 상태 확인
    boolean isLoggedIn = loginHandler.isLoggedIn(webDriver);
    if (!isLoggedIn) {
        webDriver.quit();
        webDriver = null;
        // 새 세션 생성
    } else {
        return webDriver; // 기존 세션 재사용
    }
}
```

### 11.2 세션 만료 감지

**1. 수동 로그아웃 감지**:
- JavaScript 후킹으로 브라우저의 로그아웃 버튼 클릭 감지
- `localStorage.__BUN_LOGOUT_FLAG__` 플래그 설정
- 서버에서 플래그 확인 후 DB 토큰 자동 삭제

**2. 자동 세션 만료 감지**:
- UI 요소 존재 여부로 로그인 상태 판단 (6개 중 2개 이상)
- 로그아웃 상태 감지 시 기존 세션 종료 및 새 세션 생성

### 11.3 로그인 상태 확인 기준

```java
// BunjangLoginHandler.java - checkMobileLoginStatus()
boolean hasLogoutButton = checkElementExists(driver, "//button[contains(text(),'로그아웃')]");
boolean hasNotification = checkElementExists(driver, "//a[contains(text(),'알림')]");
boolean hasMyShopDropdown = checkElementExists(driver, "//div[contains(@class,'sc-dnqmqq')]");
boolean hasAccountSettings = checkElementExists(driver, "//a[contains(text(),'계정설정')]");
boolean hasMyProducts = checkElementExists(driver, "//a[contains(text(),'내 상품')]");
boolean hasFavorites = checkElementExists(driver, "//a[contains(text(),'찜한상품')]");

// 6개 중 2개 이상 만족하면 로그인 성공
if (successCount >= 2) {
    return true;
}
```

---

**문서 작성일**: 2025년 10월 31일  
**최종 업데이트**: 2025년 10월 31일

