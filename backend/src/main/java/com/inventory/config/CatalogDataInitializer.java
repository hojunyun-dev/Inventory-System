package com.inventory.config;

import com.inventory.entity.*;
import com.inventory.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class CatalogDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            initializeCatalogData();
        }
    }

    private void initializeCatalogData() {
        // 1. 계층적 카테고리 구조 생성
        createCategoryHierarchy();
        
        // 2. 카탈로그 표준화된 제품 데이터 생성
        createCatalogProducts();
        
        // 3. 차량 호환성 데이터 생성
        createVehicleCompatibilityData();
        
        // 4. 재고 데이터 생성
        createInventoryData();
    }

    private void createCategoryHierarchy() {
        // 대분류 (Level 0)
        Category engineCategory = createCategory("엔진", "엔진 관련 부품", null, 0, 1);
        Category brakeCategory = createCategory("브레이크", "브레이크 시스템 부품", null, 0, 2);
        Category suspensionCategory = createCategory("서스펜션", "서스펜션 시스템 부품", null, 0, 3);
        Category electricalCategory = createCategory("전기", "전기 시스템 부품", null, 0, 4);
        Category filterCategory = createCategory("필터", "각종 필터 부품", null, 0, 5);

        // 중분류 (Level 1) - 엔진
        Category engineOilCategory = createCategory("엔진오일", "엔진오일 및 윤활유", engineCategory, 1, 1);
        Category engineFilterCategory = createCategory("엔진필터", "엔진 필터류", engineCategory, 1, 2);
        Category sparkPlugCategory = createCategory("점화플러그", "점화 시스템 부품", engineCategory, 1, 3);

        // 중분류 (Level 1) - 브레이크
        Category brakePadCategory = createCategory("브레이크패드", "브레이크 패드", brakeCategory, 1, 1);
        Category brakeDiscCategory = createCategory("브레이크디스크", "브레이크 디스크", brakeCategory, 1, 2);
        Category brakeFluidCategory = createCategory("브레이크액", "브레이크 유체", brakeCategory, 1, 3);

        // 소분류 (Level 2) - 엔진오일
        createCategory("5W-30", "5W-30 등급 엔진오일", engineOilCategory, 2, 1);
        createCategory("5W-40", "5W-40 등급 엔진오일", engineOilCategory, 2, 2);
        createCategory("10W-40", "10W-40 등급 엔진오일", engineOilCategory, 2, 3);

        // 소분류 (Level 2) - 브레이크패드
        createCategory("세라믹 패드", "세라믹 브레이크 패드", brakePadCategory, 2, 1);
        createCategory("메탈릭 패드", "메탈릭 브레이크 패드", brakePadCategory, 2, 2);
        createCategory("오가닉 패드", "오가닉 브레이크 패드", brakePadCategory, 2, 3);
    }

    private Category createCategory(String name, String description, Category parent, Integer level, Integer sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setParent(parent);
        category.setLevel(level);
        category.setSortOrder(sortOrder);
        category.setIsActive(true);
        return categoryRepository.save(category);
    }

    private void createCatalogProducts() {
        // 카테고리 조회
        Category engineOilCategory = categoryRepository.findByName("엔진오일").get(0);
        Category brakePadCategory = categoryRepository.findByName("브레이크패드").get(0);
        Category sparkPlugCategory = categoryRepository.findByName("점화플러그").get(0);

        // 1. 엔진오일 제품들
        createProduct("ENG-OIL-001", "현대 5W-30 합성엔진오일", "현대차 전용 5W-30 합성엔진오일", 
                     BigDecimal.valueOf(25000), BigDecimal.valueOf(15000), "HYUNDAI-5W30", 50, 10,
                     engineOilCategory, PartType.LUBRICANT, PartCondition.NEW,
                     "HY-5W30-001", "HMO-5W30", "HYUNDAI", "현대자동차",
                     "MOBIL-5W30,CASTROL-5W30", 3.5, "1L", "합성유", "투명", "한국", 12, true, false);

        createProduct("ENG-OIL-002", "기아 5W-40 합성엔진오일", "기아차 전용 5W-40 합성엔진오일", 
                     BigDecimal.valueOf(28000), BigDecimal.valueOf(17000), "KIA-5W40", 30, 5,
                     engineOilCategory, PartType.LUBRICANT, PartCondition.NEW,
                     "KIA-5W40-001", "KMO-5W40", "KIA", "기아자동차",
                     "SHELL-5W40,TOTAL-5W40", 4.0, "1L", "합성유", "투명", "한국", 12, true, false);

        // 2. 브레이크 패드 제품들
        createProduct("BRK-PAD-001", "현대 아반떼 브레이크패드 (세라믹)", "현대 아반떼 전용 세라믹 브레이크패드", 
                     BigDecimal.valueOf(85000), BigDecimal.valueOf(45000), "HYUNDAI-BRK-001", 25, 5,
                     brakePadCategory, PartType.BRAKE, PartCondition.NEW,
                     "HY-BRK-001", "HBP-CER-001", "HYUNDAI", "현대자동차",
                     "BOSCH-BRK001,TRW-BRK001", 2.1, "25x15x3cm", "세라믹", "회색", "한국", 24, true, false);

        createProduct("BRK-PAD-002", "기아 K5 브레이크패드 (메탈릭)", "기아 K5 전용 메탈릭 브레이크패드", 
                     BigDecimal.valueOf(95000), BigDecimal.valueOf(50000), "KIA-BRK-002", 20, 3,
                     brakePadCategory, PartType.BRAKE, PartCondition.NEW,
                     "KIA-BRK-002", "KBP-MET-002", "KIA", "기아자동차",
                     "BREMBO-BRK002,ATE-BRK002", 2.3, "26x16x3cm", "메탈릭", "회색", "한국", 24, true, false);

        // 3. 점화플러그 제품들
        createProduct("SPK-PLG-001", "현대 소나타 점화플러그 (이리듐)", "현대 소나타 전용 이리듐 점화플러그", 
                     BigDecimal.valueOf(45000), BigDecimal.valueOf(25000), "HYUNDAI-SPK-001", 40, 10,
                     sparkPlugCategory, PartType.IGNITION, PartCondition.NEW,
                     "HY-SPK-001", "HSP-IR-001", "HYUNDAI", "현대자동차",
                     "NGK-SPK001,DENSO-SPK001", 0.1, "14mm", "이리듐", "은색", "일본", 36, true, false);

        createProduct("SPK-PLG-002", "기아 스포티지 점화플러그 (플래티넘)", "기아 스포티지 전용 플래티넘 점화플러그", 
                     BigDecimal.valueOf(55000), BigDecimal.valueOf(30000), "KIA-SPK-002", 35, 8,
                     sparkPlugCategory, PartType.IGNITION, PartCondition.NEW,
                     "KIA-SPK-002", "KSP-PL-002", "KIA", "기아자동차",
                     "BOSCH-SPK002,CHAMPION-SPK002", 0.1, "14mm", "플래티넘", "은색", "독일", 36, true, false);

        // 4. 애프터마켓 제품들
        createProduct("AFT-FIL-001", "만필터 에어필터 (범용)", "범용 에어필터 - 대부분 차량 호환", 
                     BigDecimal.valueOf(15000), BigDecimal.valueOf(8000), "MANN-AF-001", 100, 20,
                     engineOilCategory, PartType.FILTER, PartCondition.NEW,
                     null, "MAF-UNI-001", "MANN", "만필터",
                     "BOSCH-AF001,FRAM-AF001", 0.5, "20x15x3cm", "종이", "흰색", "독일", 12, false, true);

        createProduct("AFT-OIL-001", "모빌1 5W-30 합성엔진오일", "모빌1 브랜드 5W-30 합성엔진오일", 
                     BigDecimal.valueOf(22000), BigDecimal.valueOf(12000), "MOBIL-5W30", 60, 15,
                     engineOilCategory, PartType.LUBRICANT, PartCondition.NEW,
                     null, "M1-5W30", "MOBIL", "엑슨모빌",
                     "CASTROL-5W30,SHELL-5W30", 4.0, "1L", "합성유", "투명", "미국", 12, false, true);
    }

    private void createProduct(String sku, String name, String description, BigDecimal price, BigDecimal cost,
                              String barcode, Integer quantity, Integer minimumQuantity, Category category,
                              PartType partType, PartCondition partCondition, String oemPartNumber, String aftermarketPartNumber,
                              String manufacturerCode, String manufacturerName, String crossReferenceNumbers,
                              Double weight, String dimensions, String material, String color, String countryOfOrigin,
                              Integer warrantyMonths, Boolean isOeQuality, Boolean isAftermarket) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCost(cost);
        product.setBarcode(barcode);
        product.setQuantity(quantity);
        product.setMinimumQuantity(minimumQuantity);
        product.setCategory(category);
        product.setPartType(partType);
        product.setPartCondition(partCondition);
        product.setOemPartNumber(oemPartNumber);
        product.setAftermarketPartNumber(aftermarketPartNumber);
        product.setManufacturerCode(manufacturerCode);
        product.setManufacturerName(manufacturerName);
        product.setCrossReferenceNumbers(crossReferenceNumbers);
        product.setWeight(weight);
        product.setDimensions(dimensions);
        product.setMaterial(material);
        product.setColor(color);
        product.setCountryOfOrigin(countryOfOrigin);
        product.setWarrantyMonths(warrantyMonths);
        product.setIsOeQuality(isOeQuality);
        product.setIsAftermarket(isAftermarket);
        product.setIsActive(true);
        product.setIsSerialized(false);
        
        productRepository.save(product);
    }

    private void createVehicleCompatibilityData() {
        // 현대 아반떼 호환성
        createVehicleCompatibility("현대", "아반떼", 2010, 2023, "1.6L 가솔린", "자동", "기본형", "현대 아반떼 1.6L 가솔린 엔진 호환");
        createVehicleCompatibility("현대", "아반떼", 2010, 2023, "1.6L 가솔린", "수동", "기본형", "현대 아반떼 1.6L 가솔린 엔진 호환");
        
        // 기아 K5 호환성
        createVehicleCompatibility("기아", "K5", 2015, 2023, "2.0L 가솔린", "자동", "프리미엄", "기아 K5 2.0L 가솔린 엔진 호환");
        createVehicleCompatibility("기아", "K5", 2015, 2023, "1.6L 터보", "자동", "스포츠", "기아 K5 1.6L 터보 엔진 호환");
        
        // 현대 소나타 호환성
        createVehicleCompatibility("현대", "소나타", 2014, 2023, "2.0L 가솔린", "자동", "기본형", "현대 소나타 2.0L 가솔린 엔진 호환");
        createVehicleCompatibility("현대", "소나타", 2014, 2023, "1.6L 터보", "자동", "스포츠", "현대 소나타 1.6L 터보 엔진 호환");
        
        // 기아 스포티지 호환성
        createVehicleCompatibility("기아", "스포티지", 2016, 2023, "2.0L 가솔린", "자동", "기본형", "기아 스포티지 2.0L 가솔린 엔진 호환");
        createVehicleCompatibility("기아", "스포티지", 2016, 2023, "1.6L 터보", "자동", "스포츠", "기아 스포티지 1.6L 터보 엔진 호환");
    }

    private void createVehicleCompatibility(String manufacturer, String model, Integer yearStart, Integer yearEnd,
                                           String engineType, String transmission, String trim, String notes) {
        VehicleCompatibility compatibility = new VehicleCompatibility();
        compatibility.setManufacturer(manufacturer);
        compatibility.setModel(model);
        compatibility.setYearStart(yearStart);
        compatibility.setYearEnd(yearEnd);
        compatibility.setEngineType(engineType);
        compatibility.setTransmission(transmission);
        compatibility.setTrim(trim);
        compatibility.setNotes(notes);
        
        vehicleCompatibilityRepository.save(compatibility);
    }
    
    private void createInventoryData() {
        // 모든 제품에 대해 재고 데이터 생성
        var products = productRepository.findAll();
        
        for (Product product : products) {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(generateRandomQuantity(product.getName()));
            inventory.setWarehouseLocation(generateWarehouseLocation());
            
            inventoryRepository.save(inventory);
        }
    }
    
    private Integer generateRandomQuantity(String productName) {
        // 제품명에 따라 다른 수량 범위 설정
        if (productName.contains("엔진오일")) {
            return 50 + (int)(Math.random() * 100); // 50-150개
        } else if (productName.contains("브레이크")) {
            return 20 + (int)(Math.random() * 30); // 20-50개
        } else if (productName.contains("필터")) {
            return 30 + (int)(Math.random() * 70); // 30-100개
        } else if (productName.contains("점화")) {
            return 40 + (int)(Math.random() * 60); // 40-100개
        } else {
            return 10 + (int)(Math.random() * 40); // 10-50개
        }
    }
    
    private String generateWarehouseLocation() {
        String[] locations = {"A-01-01", "A-01-02", "A-02-01", "B-01-01", "B-01-02", "C-01-01", "C-02-01"};
        return locations[(int)(Math.random() * locations.length)];
    }
}
