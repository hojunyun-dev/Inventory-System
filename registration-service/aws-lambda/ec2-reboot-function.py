import json
import boto3
import os
import logging

# 로깅 설정
logger = logging.getLogger()
logger.setLevel(logging.INFO)

# AWS 클라이언트 초기화
ec2_client = boto3.client('ec2')
s3_client = boto3.client('s3')

def lambda_handler(event, context):
    """
    S3 이벤트로 트리거되는 EC2 리부트 함수
    - reboot/ 경로에 파일이 업로드되면 EC2 인스턴스 중지/시작
    """
    try:
        # 환경 변수에서 설정 가져오기
        instance_id = os.environ['INSTANCE_ID']
        bucket_name = os.environ['S3_BUCKET']
        
        logger.info(f"EC2 reboot triggered for instance: {instance_id}")
        
        # EC2 인스턴스 상태 확인
        response = ec2_client.describe_instances(InstanceIds=[instance_id])
        instance_state = response['Reservations'][0]['Instances'][0]['State']['Name']
        
        logger.info(f"Current instance state: {instance_state}")
        
        if instance_state == 'running':
            # 인스턴스 중지
            logger.info("Stopping EC2 instance...")
            ec2_client.stop_instances(InstanceIds=[instance_id])
            
            # 중지 완료 대기
            waiter = ec2_client.get_waiter('instance_stopped')
            waiter.wait(InstanceIds=[instance_id])
            
            logger.info("EC2 instance stopped successfully")
            
            # 인스턴스 시작
            logger.info("Starting EC2 instance...")
            ec2_client.start_instances(InstanceIds=[instance_id])
            
            # 시작 완료 대기
            waiter = ec2_client.get_waiter('instance_running')
            waiter.wait(InstanceIds=[instance_id])
            
            logger.info("EC2 instance started successfully")
            
            return {
                'statusCode': 200,
                'body': json.dumps({
                    'message': 'EC2 instance rebooted successfully',
                    'instanceId': instance_id,
                    'newState': 'running'
                })
            }
        else:
            logger.warning(f"Instance is not in running state: {instance_state}")
            return {
                'statusCode': 400,
                'body': json.dumps({
                    'message': f'Instance is not running: {instance_state}',
                    'instanceId': instance_id
                })
            }
            
    except Exception as e:
        logger.error(f"Error rebooting EC2 instance: {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps({
                'message': f'Error rebooting EC2 instance: {str(e)}',
                'instanceId': instance_id
            })
        }
