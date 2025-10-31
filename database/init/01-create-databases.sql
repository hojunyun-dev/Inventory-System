-- 데이터베이스 생성 스크립트
-- 자동차 부품 재고 관리 시스템용 데이터베이스들

-- 메인 재고 관리 데이터베이스
CREATE DATABASE IF NOT EXISTS inventory_system 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 토큰 관리 데이터베이스
CREATE DATABASE IF NOT EXISTS token_management_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 등록 서비스 데이터베이스
CREATE DATABASE IF NOT EXISTS registration_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- 사용자 권한 설정
GRANT ALL PRIVILEGES ON inventory_system.* TO 'inventory'@'%';
GRANT ALL PRIVILEGES ON token_management_db.* TO 'inventory'@'%';
GRANT ALL PRIVILEGES ON registration_db.* TO 'inventory'@'%';

-- 권한 새로고침
FLUSH PRIVILEGES;

-- 사용할 데이터베이스 선택
USE inventory_system;

-- 상품 이미지 테이블 생성 (리사이징 지원)
CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    image_name VARCHAR(255) NOT NULL,
    image_data LONGBLOB NOT NULL,
    image_type VARCHAR(50) NOT NULL,
    image_size BIGINT NOT NULL,
    image_category ENUM('ORIGINAL', 'THUMBNAIL', 'REGISTRATION') NOT NULL,
    original_image_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    FOREIGN KEY (original_image_id) REFERENCES product_images(id) ON DELETE CASCADE,
    INDEX idx_product_images_product_id (product_id),
    INDEX idx_product_images_created_at (created_at),
    INDEX idx_product_images_category (image_category),
    INDEX idx_product_images_original_id (original_image_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
