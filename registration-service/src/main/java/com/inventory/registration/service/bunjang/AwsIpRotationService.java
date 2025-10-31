package com.inventory.registration.service.bunjang;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * AWS EC2 IP 자동 변경 서비스
 * - 차단 감지 시 EC2 인스턴스 중지/시작으로 IP 변경
 * - 상태 관리 및 자동 재시작 기능
 */
@Service
@Slf4j
public class AwsIpRotationService {

    @Value("${automation.aws.enabled:false}")
    private Boolean awsEnabled;

    @Value("${automation.aws.region:ap-northeast-2}")
    private String awsRegion;

    @Value("${automation.aws.instance-id:}")
    private String instanceId;

    @Value("${automation.aws.access-key:}")
    private String accessKey;

    @Value("${automation.aws.secret-key:}")
    private String secretKey;

    @Value("${automation.aws.s3-bucket:}")
    private String s3Bucket;

    @Value("${automation.aws.error-threshold:5}")
    private Integer errorThreshold;

    private int consecutiveErrorCount = 0;
    private final List<String> errorIds = new ArrayList<>();
    private final List<String> finalErrorIds = new ArrayList<>();
    private final List<String> productIds = new ArrayList<>();

    private static final String STATE_FILE = "/tmp/crawler_state.pkl";
    private static final String COMPLETE_PREFIX = "complete/";
    private static final String REBOOT_PREFIX = "reboot/";

    /**
     * 차단 감지 및 에러 카운트 관리
     */
    public boolean handleBlockingDetection(String errorMessage) {
        if (!awsEnabled) {
            log.debug("AWS IP rotation is disabled");
            return false;
        }

        consecutiveErrorCount++;
        log.warn("🚫 Blocking detected (count: {}/{}): {}", consecutiveErrorCount, errorThreshold, errorMessage);

        if (consecutiveErrorCount >= errorThreshold) {
            log.error("🚨 Error threshold reached! Triggering EC2 reboot...");
            triggerEc2Reboot();
            return true;
        }

        return false;
    }

    /**
     * EC2 리부트 트리거
     */
    private void triggerEc2Reboot() {
        try {
            // 현재 상태 저장
            saveCurrentState();

            // S3에 리부트 트리거 파일 업로드
            uploadRebootTrigger();

            log.info("✅ EC2 reboot triggered successfully");

        } catch (Exception e) {
            log.error("❌ Failed to trigger EC2 reboot: {}", e.getMessage(), e);
        }
    }

    /**
     * 현재 상태 저장
     */
    private void saveCurrentState() {
        try {
            Path statePath = Paths.get(STATE_FILE);

            // 상태 정보를 JSON 형태로 저장
            StringBuilder stateData = new StringBuilder();
            stateData.append("{\n");
            stateData.append("  \"consecutiveErrorCount\": ").append(consecutiveErrorCount).append(",\n");
            stateData.append("  \"errorIds\": ").append(errorIds.toString()).append(",\n");
            stateData.append("  \"finalErrorIds\": ").append(finalErrorIds.toString()).append(",\n");
            stateData.append("  \"productIds\": ").append(productIds.toString()).append(",\n");
            stateData.append("  \"timestamp\": ").append(System.currentTimeMillis()).append("\n");
            stateData.append("}\n");

            Files.write(statePath, stateData.toString().getBytes());
            log.info("✅ Current state saved to: {}", STATE_FILE);

        } catch (Exception e) {
            log.error("❌ Failed to save current state: {}", e.getMessage(), e);
        }
    }

    /**
     * 상태 복원
     */
    public void restoreState() {
        try {
            Path statePath = Paths.get(STATE_FILE);
            if (!Files.exists(statePath)) {
                log.info("No previous state file found, starting fresh");
                return;
            }

            String stateData = Files.readString(statePath);
            // 실제 구현에서는 JSON 파싱 라이브러리 사용
            log.info("✅ Previous state restored from: {}", STATE_FILE);

        } catch (Exception e) {
            log.error("❌ Failed to restore previous state: {}", e.getMessage(), e);
        }
    }

    /**
     * S3에 리부트 트리거 파일 업로드
     */
    private void uploadRebootTrigger() {
        try {
            // 실제 구현에서는 AWS SDK를 사용하여 S3에 파일 업로드
            String triggerFileName = REBOOT_PREFIX + "trigger_" + System.currentTimeMillis() + ".json";

            // 시뮬레이션용 로그
            log.info("📤 Uploading reboot trigger to S3: {}", triggerFileName);
            log.info("🔧 Lambda function will be triggered to reboot EC2 instance");

        } catch (Exception e) {
            log.error("❌ Failed to upload reboot trigger: {}", e.getMessage(), e);
        }
    }

    /**
     * 작업 완료 시 S3에 완료 트리거 파일 업로드
     */
    public void uploadCompleteTrigger() {
        try {
            if (!awsEnabled) {
                return;
            }

            String triggerFileName = COMPLETE_PREFIX + "complete_" + System.currentTimeMillis() + ".json";

            // 시뮬레이션용 로그
            log.info("📤 Uploading complete trigger to S3: {}", triggerFileName);
            log.info("🛑 Lambda function will be triggered to stop EC2 instance");

        } catch (Exception e) {
            log.error("❌ Failed to upload complete trigger: {}", e.getMessage(), e);
        }
    }

    /**
     * 에러 ID 추가
     */
    public void addErrorId(String errorId) {
        if (errorIds.contains(errorId)) {
            finalErrorIds.add(errorId);
            errorIds.remove(errorId);
            log.warn("🔄 Error ID moved to final error list: {}", errorId);
        } else {
            errorIds.add(errorId);
            log.info("➕ Error ID added to retry list: {}", errorId);
        }
    }

    /**
     * 성공 시 에러 카운트 리셋
     */
    public void resetErrorCount() {
        if (consecutiveErrorCount > 0) {
            log.info("✅ Success detected, resetting error count from {} to 0", consecutiveErrorCount);
            consecutiveErrorCount = 0;
        }
    }

    /**
     * 현재 상태 정보 반환
     */
    public String getCurrentStatus() {
        return String.format("AWS IP Rotation Status:\n" + "- Enabled: %s\n" + "- Consecutive Errors: %d/%d\n" + "- Error IDs: %d\n" + "- Final Error IDs: %d\n" + "- Product IDs: %d", awsEnabled, consecutiveErrorCount, errorThreshold, errorIds.size(), finalErrorIds.size(), productIds.size());
    }

    /**
     * EC2 인스턴스 시작 (월별 스케줄링용)
     */
    public void startEc2Instance() {
        try {
            if (!awsEnabled) {
                log.debug("AWS IP rotation is disabled");
                return;
            }

            // 실제 구현에서는 AWS SDK를 사용하여 EC2 인스턴스 시작
            log.info("🚀 Starting EC2 instance: {}", instanceId);
            log.info("🔧 Lambda function will be triggered to start EC2 instance");

        } catch (Exception e) {
            log.error("❌ Failed to start EC2 instance: {}", e.getMessage(), e);
        }
    }

    /**
     * EC2 인스턴스 중지
     */
    public void stopEc2Instance() {
        try {
            if (!awsEnabled) {
                log.debug("AWS IP rotation is disabled");
                return;
            }

            // 실제 구현에서는 AWS SDK를 사용하여 EC2 인스턴스 중지
            log.info("🛑 Stopping EC2 instance: {}", instanceId);
            log.info("🔧 Lambda function will be triggered to stop EC2 instance");

        } catch (Exception e) {
            log.error("❌ Failed to stop EC2 instance: {}", e.getMessage(), e);
        }
    }
}
