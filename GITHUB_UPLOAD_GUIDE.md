# 🚀 GitHub 업로드 가이드

이 가이드는 로컬 프로젝트를 GitHub 레포지토리에 업로드하는 방법을 설명합니다.

## 📋 사전 준비

### 1. GitHub 레포지토리 생성 확인
- [ ] GitHub에서 레포지토리 생성 완료
- [ ] 레포지토리 URL 확인 (예: `https://github.com/username/inventory-system.git`)

### 2. Git 설치 확인
```bash
git --version
```

### 3. Git 사용자 정보 설정 (최초 1회)
```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

## 🔧 단계별 업로드 과정

### 1단계: 프로젝트 디렉토리로 이동
```bash
cd /home/code/바탕화면/inventory-system
```

### 2단계: Git 저장소 초기화
```bash
git init
```

### 3단계: 원격 저장소 연결
```bash
# YOUR_USERNAME과 YOUR_REPOSITORY_NAME을 실제 값으로 변경하세요
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPOSITORY_NAME.git
```

### 4단계: 파일 상태 확인
```bash
git status
```

### 5단계: 모든 파일 추가
```bash
git add .
```

### 6단계: 첫 번째 커밋 생성
```bash
git commit -m "Initial commit: 자동차 부품 재고 관리 시스템

- 마이크로서비스 아키텍처 구현 (3개 서비스)
- 토큰 관리 서비스 구현 완료
- 등록 서비스 구현 완료
- API 연동 및 데이터 동기화 구현
- React 프론트엔드 구현
- UI 테스트 가이드 작성 (185개 항목)"
```

### 7단계: GitHub에 업로드
```bash
git push -u origin main
```

## 🔄 이후 업데이트 과정

### 변경사항 업로드
```bash
# 변경된 파일 확인
git status

# 변경된 파일 추가
git add .

# 커밋 생성
git commit -m "커밋 메시지"

# GitHub에 업로드
git push origin main
```

## 📝 커밋 메시지 가이드

### 좋은 커밋 메시지 예시
```bash
git commit -m "feat: 토큰 관리 서비스 연동 구현

- TokenManagementService 클래스 추가
- OAuth 2.0 토큰 발급/갱신 로직 구현
- 플랫폼별 토큰 관리 기능 추가

Resolves: #123"
```

### 커밋 타입 가이드
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅, 세미콜론 누락 등
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드 추가
- `chore`: 빌드 과정 또는 보조 기능 수정

## 🛠️ 문제 해결

### 1. 인증 문제
```bash
# GitHub Personal Access Token 사용
git remote set-url origin https://YOUR_TOKEN@github.com/USERNAME/REPOSITORY.git
```

### 2. 브랜치 충돌
```bash
# 원격 저장소의 변경사항 가져오기
git pull origin main

# 충돌 해결 후 다시 푸시
git push origin main
```

### 3. 대용량 파일 문제
```bash
# .gitignore에 추가할 파일들
echo "*.log" >> .gitignore
echo "node_modules/" >> .gitignore
echo "target/" >> .gitignore
```

## 📊 레포지토리 관리 팁

### 1. 브랜치 전략
```bash
# 개발 브랜치 생성
git checkout -b develop

# 기능 브랜치 생성
git checkout -b feature/new-feature

# 브랜치 병합
git checkout main
git merge feature/new-feature
```

### 2. 태그 생성 (릴리스)
```bash
# 태그 생성
git tag -a v1.0.0 -m "Release version 1.0.0"

# 태그 푸시
git push origin v1.0.0
```

## 🔍 업로드 확인

### 1. GitHub 웹사이트에서 확인
- 레포지토리 페이지 방문
- 파일 목록 확인
- README.md 렌더링 확인

### 2. 로컬에서 확인
```bash
# 원격 저장소 정보 확인
git remote -v

# 최신 상태 확인
git status
```

## 📞 도움이 필요한 경우

1. **Git 명령어 도움말**: `git help [command]`
2. **GitHub 문서**: https://docs.github.com/
3. **Git 튜토리얼**: https://learngitbranching.js.org/

---

**주의사항**: 
- 민감한 정보(API 키, 비밀번호 등)는 절대 커밋하지 마세요
- .env 파일은 .gitignore에 포함되어 있습니다
- 대용량 파일은 Git LFS를 사용하세요
