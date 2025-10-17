package com.inventory.registration.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductData {
    
    // 기본 상품 정보
    private String name;
    private String description;
    private Integer price;
    private String category;
    private String condition; // 새상품, 중고, 거의새것 등
    
    // 이미지 정보
    private List<String> imageUrls;
    private List<String> imagePaths; // 로컬 파일 경로
    
    // 위치 및 배송 정보
    private String location;
    private String address;
    private boolean deliveryAvailable;
    private Integer deliveryFee;
    
    // 거래 정보
    private String tradeMethod; // 직거래, 택배, 둘다
    private String contactMethod; // 채팅, 전화 등
    
    // 플랫폼별 추가 정보
    private String platform;
    private String subCategory;
    private List<String> tags;
    private String brand;
    private String model;
    
    // 자동화 로그인 정보 (보안상 실제 운영에서는 암호화 필요)
    private String username;
    private String password;
    private String phoneNumber;
    private String verificationCode;
    
    // 메타 정보
    private LocalDateTime createdAt;
    private String source; // 재고관리시스템에서 온 데이터인지 식별
    private String externalId; // 외부 시스템의 상품 ID
}
