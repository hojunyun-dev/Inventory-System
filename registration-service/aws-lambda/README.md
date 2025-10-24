# AWS EC2 IP 자동 변경 시스템

이 시스템은 번개장터 자동화 시 IP 차단을 우회하기 위해 AWS EC2 인스턴스의 IP를 자동으로 변경하는 시스템입니다.

## 🏗️ 시스템 아키텍처

```
크롤링 스크립트 → 차단 감지 → S3 업로드 → Lambda 트리거 → EC2 중지/시작 → 새 IP로 재시작
```

## 📋 구성 요소

### 1. Lambda 함수들
- **EC2RebootFunction**: EC2 인스턴스 중지/시작 (IP 변경)
- **EC2StartFunction**: EC2 인스턴스 시작 (월별 스케줄링)
- **EC2StopFunction**: EC2 인스턴스 중지 (작업 완료 시)

### 2. S3 버킷
- **reboot/**: 리부트 트리거 파일 저장
- **complete/**: 완료 트리거 파일 저장

### 3. EventBridge
- 월별 EC2 인스턴스 자동 시작 스케줄링

## 🚀 설정 방법

### 1. AWS IAM 정책 설정
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ec2:StartInstances",
                "ec2:StopInstances",
                "ec2:DescribeInstances"
            ],
            "Resource": "arn:aws:ec2:region:account:instance/i-xxxxxxxxx"
        },
        {
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:DeleteObject"
            ],
            "Resource": "arn:aws:s3:::your-bucket-name/*"
        }
    ]
}
```

### 2. Lambda 함수 배포
```bash
# EC2 리부트 함수
zip ec2-reboot-function.zip ec2-reboot-function.py
aws lambda create-function \
    --function-name EC2RebootFunction \
    --runtime python3.9 \
    --role arn:aws:iam::account:role/lambda-execution-role \
    --handler ec2-reboot-function.lambda_handler \
    --zip-file fileb://ec2-reboot-function.zip

# 환경 변수 설정
aws lambda update-function-configuration \
    --function-name EC2RebootFunction \
    --environment Variables='{
        "INSTANCE_ID": "i-xxxxxxxxx",
        "S3_BUCKET": "your-bucket-name"
    }'
```

### 3. S3 이벤트 트리거 설정
```bash
# S3 버킷에 이벤트 알림 설정
aws s3api put-bucket-notification-configuration \
    --bucket your-bucket-name \
    --notification-configuration '{
        "LambdaConfigurations": [
            {
                "Id": "RebootTrigger",
                "LambdaFunctionArn": "arn:aws:lambda:region:account:function:EC2RebootFunction",
                "Events": ["s3:ObjectCreated"],
                "Filter": {
                    "Key": {
                        "FilterRules": [
                            {
                                "Name": "prefix",
                                "Value": "reboot/"
                            }
                        ]
                    }
                }
            },
            {
                "Id": "StopTrigger",
                "LambdaFunctionArn": "arn:aws:lambda:region:account:function:EC2StopFunction",
                "Events": ["s3:ObjectCreated"],
                "Filter": {
                    "Key": {
                        "FilterRules": [
                            {
                                "Name": "prefix",
                                "Value": "complete/"
                            }
                        ]
                    }
                }
            }
        ]
    }'
```

### 4. EventBridge 스케줄 설정
```bash
# 월별 EC2 시작 스케줄
aws events put-rule \
    --name "MonthlyEC2Start" \
    --schedule-expression "rate(30 days)" \
    --description "Monthly EC2 instance start"

aws events put-targets \
    --rule "MonthlyEC2Start" \
    --targets "Id"="1","Arn"="arn:aws:lambda:region:account:function:EC2StartFunction"
```

### 5. crontab 설정
EC2 인스턴스에서 다음 명령어로 자동 재시작 설정:
```bash
# crontab 편집
crontab -e

# 다음 라인 추가
@reboot sleep 60 && cd /path/to/your/project && mvn spring-boot:run >> /var/log/crawler.log 2>&1
```

## ⚙️ 환경 변수 설정

### application.yml
```yaml
automation:
  aws:
    enabled: true
    region: ap-northeast-2
    instance-id: i-xxxxxxxxx
    access-key: ${AWS_ACCESS_KEY}
    secret-key: ${AWS_SECRET_KEY}
    s3-bucket: your-bucket-name
    error-threshold: 5
```

### .env 파일
```bash
AWS_ACCESS_KEY=your-access-key
AWS_SECRET_KEY=your-secret-key
AWS_IP_ROTATION_ENABLED=true
```

## 🔄 작동 플로우

1. **크롤링 시작**: 번개장터 자동화 시작
2. **차단 감지**: 연속 에러 5회 발생 시 차단으로 판단
3. **상태 저장**: 현재 진행 상황을 로컬 파일에 저장
4. **S3 업로드**: `reboot/trigger_timestamp.json` 파일 업로드
5. **Lambda 트리거**: S3 이벤트로 EC2RebootFunction 실행
6. **EC2 리부트**: 인스턴스 중지 → 시작 (새 IP 할당)
7. **자동 재시작**: crontab으로 크롤링 스크립트 자동 재시작
8. **상태 복원**: 이전 진행 상황 복원하여 계속 진행
9. **완료 시**: `complete/complete_timestamp.json` 업로드
10. **EC2 중지**: EC2StopFunction으로 인스턴스 자동 중지

## 📊 모니터링

### CloudWatch 로그
- Lambda 함수 실행 로그 확인
- EC2 인스턴스 상태 변경 로그

### S3 버킷
- 트리거 파일 업로드/삭제 확인
- 작업 상태 추적

## ⚠️ 주의사항

1. **비용 관리**: EC2 인스턴스 중지/시작으로 인한 비용 발생
2. **데이터 보존**: EBS 볼륨은 중지 시에도 데이터 보존됨
3. **네트워크 설정**: 보안 그룹, 서브넷 등 네트워크 설정 확인
4. **권한 관리**: IAM 역할 및 정책 적절히 설정
5. **로깅**: 모든 작업에 대한 상세 로깅 설정

## 🛠️ 트러블슈팅

### 자주 발생하는 문제
1. **Lambda 권한 오류**: IAM 역할에 EC2 권한 추가
2. **S3 이벤트 미작동**: 버킷 알림 설정 확인
3. **EC2 상태 확인 실패**: 인스턴스 ID 및 리전 확인
4. **자동 재시작 실패**: crontab 설정 및 경로 확인

### 로그 확인 명령어
```bash
# Lambda 로그 확인
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/

# EC2 인스턴스 상태 확인
aws ec2 describe-instances --instance-ids i-xxxxxxxxx

# S3 버킷 내용 확인
aws s3 ls s3://your-bucket-name/reboot/
aws s3 ls s3://your-bucket-name/complete/
```
