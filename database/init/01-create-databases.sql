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
