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

### 1. 단순화된 브랜치 전략
```
main (메인 브랜치)
├── feature/microservice-refactoring  # 4단계: 마이크로서비스 리팩토링
├── feature/database-migration        # 4단계: DB 전환 작업 (보류)
├── feature/testing                   # 5단계: 통합 테스트
└── feature/deployment                # 5단계: 배포 설정
```

### 2. 작업 단계별 브랜치 관리
- **현재 진행 중**: `feature/microservice-refactoring` (4단계)
- **완료 후**: `feature/testing` (5단계)

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
# 최신 변경사항 가져오기
git checkout main
git pull origin main

# 작업 브랜치 전환
git checkout feature/current-work
git rebase main  # 선택사항
```

### 작업 중간 커밋
```bash
# Cursor GUI 사용 (권장)
# 1. Source Control 패널에서 변경사항 확인
# 2. 스테이징할 파일 선택
# 3. 커밋 메시지 작성
# 4. 커밋 버튼 클릭
```

### 작업 완료 시
```bash
# 작업 브랜치에서 최종 커밋
git add .
git commit -m "feat: [기능명] 구현 완료"

# main 브랜치로 병합
git checkout main
git merge feature/current-work
git push origin main
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

## 🎯 권장 워크플로우

1. **작업 시작**: `feature/작업명` 브랜치 생성
2. **작업 중**: Cursor GUI로 자주 커밋
3. **작업 완료**: main 브랜치로 병합
4. **정리**: 작업 브랜치 삭제

이 방식으로 **개인 개발에 최적화된 Git 워크플로우**를 구축할 수 있습니다!
