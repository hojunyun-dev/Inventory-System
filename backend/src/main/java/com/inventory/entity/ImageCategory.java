package com.inventory.entity;

/**
 * 이미지 카테고리 열거형
 * - 이미지의 용도에 따라 구분
 */
public enum ImageCategory {
    /**
     * 원본 이미지
     * - 사용자가 업로드한 원본 고품질 이미지
     * - 최대 해상도와 품질 유지
     */
    ORIGINAL("원본"),
    
    /**
     * 썸네일 이미지
     * - 목록 표시용 작은 이미지
     * - 빠른 로딩을 위한 최적화된 크기
     */
    THUMBNAIL("썸네일"),
    
    /**
     * 등록용 이미지
     * - 번개장터 등록에 최적화된 이미지
     * - 권장 크기와 품질로 리사이징
     */
    REGISTRATION("등록용");

    private final String description;

    ImageCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
