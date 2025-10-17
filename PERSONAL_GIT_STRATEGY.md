# 🚀 개인 개발을 위한 Git 전략

기존 팀 개발 환경에서 개인 개발 환경으로 전환하면서 최적화된 Git 워크플로우 가이드입니다.

## 📋 기존 vs 현재 환경 비교

| 구분 | 기존 (팀 개발) | 현재 (개인 개발) |
|------|----------------|------------------|
| **IDE** | IntelliJ IDEA | Cursor |
| **메인 브랜치** | `dev` | `main` |
| **병합 방식** | PR + 승인 | 직접 병합 |
| **리뷰 과정** | 필수 (2명 이상) | 생략 |
| **브랜치 전략** | 도메인별 브랜치 | 기능별 브랜치 |

## 🎯 개인 개발 최적화 전략

### 1. 단순화된 브랜치 전략 (권장)
```
main (안정적인 메인 브랜치 - GitHub에 배포)
└── develop (개발 작업 브랜치 - 모든 개발 작업)
```

### 2. 브랜치 역할 정의
- **main**: 
  - 안정적이고 배포 가능한 코드
  - GitHub에서 메인 브랜치로 사용
  - 주요 마일스톤 완료 시만 병합
  
- **develop**: 
  - 모든 개발 작업 진행
  - 기능 개발, 버그 수정, 리팩토링
  - 일상적인 커밋과 푸시

## 🔧 Cursor IDE Git 워크플로우

### 방법 1: Cursor GUI 사용 (권장)
1. **Source Control 패널** 사용 (Ctrl+Shift+G)
2. **변경사항 확인** → **스테이징** → **커밋** → **푸시**
3. **브랜치 전환** 및 **병합** GUI로 처리

### 방법 2: Cursor 터미널 사용
1. **통합 터미널** 열기 (Ctrl+`)
2. **Git 명령어** 직접 실행
3. **IntelliJ와 유사한** 워크플로우

### 방법 3: 하이브리드 방식 (최적)
- **일상 작업**: Cursor GUI 사용
- **복잡한 작업**: 터미널 명령어 사용
- **브랜치 관리**: GUI로 시각적 관리

## 📝 단계별 Git 명령어 가이드

### 현재 상황 (4단계 진행 중)

#### 1. 현재 작업 브랜치 생성
```bash
# 현재 main 브랜치에서
git checkout -b feature/api-integration

# 또는 Cursor GUI에서
# Source Control → 브랜치 아이콘 → 새 브랜치 생성
```

#### 2. 작업 진행 중 커밋
```bash
# 변경사항 확인
git status

# 파일 스테이징
git add .

# 커밋 생성
git commit -m "feat: API 연동 서비스 구현

- TokenManagementService 연동 완료
- RegistrationService 연동 완료  
- PlatformIntegrationService 구현 완료
- WebClient 설정 추가 필요"

# 원격 저장소에 푸시
git push origin feature/api-integration
```

#### 3. 작업 완료 후 병합
```bash
# main 브랜치로 전환
git checkout main

# 작업 브랜치 병합
git merge feature/api-integration

# 원격 저장소에 푸시
git push origin main

# 작업 브랜치 삭제 (선택사항)
git branch -d feature/api-integration
git push origin --delete feature/api-integration
```

## 🎨 Cursor IDE Git 기능 활용

### Source Control 패널 활용
1. **변경사항 확인**: 파일별 변경 내용 시각적 확인
2. **선택적 스테이징**: 파일별 또는 라인별 스테이징
3. **커밋 메시지**: 인라인 커밋 메시지 작성
4. **브랜치 관리**: 브랜치 생성/전환/병합 GUI로 처리

### Git 확장 기능
1. **GitLens**: 커밋 히스토리 및 작성자 정보 표시
2. **Git Graph**: 브랜치 관계 시각화
3. **Git History**: 파일별 변경 히스토리 추적

## 📊 작업 단계별 Git 전략

### 4단계: 재고관리 시스템 리팩토링
```bash
# 현재 진행 중인 API 연동 작업
feature/api-integration
├── TokenManagementService 연동 ✅
├── RegistrationService 연동 ✅  
├── PlatformIntegrationService 구현 ✅
├── WebClient 설정 최적화 🔄
└── 서비스 간 통신 테스트 ⏳

# 다음 예정 작업
feature/data-sync
├── 데이터 동기화 로직 구현
├── 이벤트 기반 동기화
└── 서비스 간 데이터 일관성 보장
```

### 5단계: 통합 테스트 및 배포
```bash
feature/testing
├── 통합 테스트 구현
├── 성능 테스트
└── 배포 환경 설정

feature/deployment  
├── Docker 설정
├── Kubernetes 배포
└── 모니터링 설정
```

## 🔄 일상적인 Git 워크플로우

### 매일 작업 시작 시
```bash
# develop 브랜치에서 작업 (일반적인 경우)
git checkout develop
git pull origin develop  # 다른 기기에서 작업한 경우

# 또는 main에서 최신 변경사항 확인
git checkout main
git pull origin main
git checkout develop
```

### 작업 중간 커밋 (develop 브랜치에서)
```bash
# Cursor GUI 사용 (권장)
# 1. Source Control 패널에서 변경사항 확인
# 2. 스테이징할 파일 선택
# 3. 커밋 메시지 작성
# 4. 커밋 버튼 클릭

# 또는 터미널 사용
git add .
git commit -m "feat: [기능명] 구현 완료"
git push origin develop
```

### 주요 마일스톤 완료 시 (main 브랜치 병합)
```bash
# develop에서 main으로 병합
git checkout main
git merge develop
git push origin main

# 태그 생성 (선택사항)
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

## 🛠️ 문제 해결 및 팁

### 1. 브랜치 충돌 해결
```bash
# 충돌 발생 시
git status  # 충돌 파일 확인
# Cursor에서 충돌 해결 후
git add .
git commit -m "resolve: 브랜치 충돌 해결"
```

### 2. 커밋 메시지 규칙
```bash
# 기능 추가
feat: 새로운 기능 추가

# 버그 수정  
fix: 버그 수정

# 문서 수정
docs: 문서 업데이트

# 리팩토링
refactor: 코드 리팩토링
```

### 3. 작업 백업
```bash
# 작업 중간 백업 (원격 저장소)
git add .
git commit -m "WIP: 작업 중간 백업"
git push origin feature/current-work
```

## 📈 Git 히스토리 관리

### 깔끔한 커밋 히스토리 유지
```bash
# 여러 커밋을 하나로 합치기
git rebase -i HEAD~3

# 커밋 메시지 수정
git commit --amend -m "새로운 커밋 메시지"
```

### 태그를 통한 버전 관리
```bash
# 주요 완료 시점에 태그 생성
git tag -a v1.0.0 -m "4단계 API 연동 완료"
git push origin v1.0.0
```

---

## 🎯 권장 워크플로우 (개인 개발)

### 일상적인 개발 작업
1. **작업 시작**: `develop` 브랜치에서 작업
2. **작업 중**: Cursor GUI로 자주 커밋 및 푸시
3. **작업 완료**: `develop` 브랜치에 계속 커밋

### 주요 마일스톤 완료 시
1. **안정성 확인**: `develop` 브랜치에서 테스트 완료
2. **main 병합**: `develop` → `main` 병합
3. **태그 생성**: 버전 태그 생성 (v1.0.0, v1.1.0 등)
4. **배포**: GitHub에서 릴리스 노트 작성

### 향후 팀 개발 확장 시
1. **feature 브랜치**: `develop`에서 `feature/기능명` 브랜치 생성
2. **PR 생성**: `feature/기능명` → `develop` Pull Request
3. **코드 리뷰**: 팀원들의 리뷰 후 병합
4. **릴리스**: `develop` → `main` 병합 후 배포

이 방식으로 **개인 개발부터 팀 개발까지 확장 가능한 Git 워크플로우**를 구축할 수 있습니다!
