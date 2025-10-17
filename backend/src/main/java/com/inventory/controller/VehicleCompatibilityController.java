package com.inventory.controller;

import com.inventory.entity.VehicleCompatibility;
import com.inventory.repository.VehicleCompatibilityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/vehicle-compatibilities")
public class VehicleCompatibilityController {

    private final VehicleCompatibilityRepository vehicleCompatibilityRepository;

    public VehicleCompatibilityController(VehicleCompatibilityRepository vehicleCompatibilityRepository) {
        this.vehicleCompatibilityRepository = vehicleCompatibilityRepository;
    }

    @GetMapping
    public ResponseEntity<List<VehicleCompatibility>> getAllVehicleCompatibilities() {
        List<VehicleCompatibility> compatibilities = vehicleCompatibilityRepository.findAll();
        return ResponseEntity.ok(compatibilities);
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleCompatibility>> searchVehicleCompatibilities(
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year
    ) {
        List<VehicleCompatibility> compatibilities;
        
        if (manufacturer != null && !manufacturer.isEmpty() && 
            model != null && !model.isEmpty() && year != null) {
            compatibilities = vehicleCompatibilityRepository.findByManufacturerAndModelAndYear(manufacturer, model, year);
        } else if (manufacturer != null && !manufacturer.isEmpty() && 
                   model != null && !model.isEmpty()) {
            compatibilities = vehicleCompatibilityRepository.findByManufacturerIgnoreCaseAndModelIgnoreCase(manufacturer, model);
        } else if (manufacturer != null && !manufacturer.isEmpty()) {
            compatibilities = vehicleCompatibilityRepository.findByManufacturerIgnoreCase(manufacturer);
        } else if (year != null) {
            compatibilities = vehicleCompatibilityRepository.findByYearRange(year);
        } else {
            compatibilities = vehicleCompatibilityRepository.findAll();
        }
        
        return ResponseEntity.ok(compatibilities);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleCompatibility> getVehicleCompatibilityById(@PathVariable Long id) {
        Optional<VehicleCompatibility> compatibility = vehicleCompatibilityRepository.findById(id);
        return compatibility.map(ResponseEntity::ok)
                          .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<VehicleCompatibility> createVehicleCompatibility(@RequestBody VehicleCompatibility compatibility) {
        VehicleCompatibility savedCompatibility = vehicleCompatibilityRepository.save(compatibility);
        return ResponseEntity.ok(savedCompatibility);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleCompatibility> updateVehicleCompatibility(
            @PathVariable Long id, 
            @RequestBody VehicleCompatibility compatibility) {
        if (!vehicleCompatibilityRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        compatibility.setId(id);
        VehicleCompatibility updatedCompatibility = vehicleCompatibilityRepository.save(compatibility);
        return ResponseEntity.ok(updatedCompatibility);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVehicleCompatibility(@PathVariable Long id) {
        if (!vehicleCompatibilityRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vehicleCompatibilityRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<List<Map<String, Object>>> getCompatibleProducts(@PathVariable Long id) {
        Optional<VehicleCompatibility> compatibility = vehicleCompatibilityRepository.findById(id);
        if (!compatibility.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        VehicleCompatibility vc = compatibility.get();
        List<Map<String, Object>> compatibleProducts = new ArrayList<>();

        // 차량 정보에 따른 호환 부품 시뮬레이션
        if (vc.getManufacturer().equals("현대") && vc.getModel().equals("소나타")) {
            if (vc.getEngineType().contains("2.0L")) {
                compatibleProducts.add(createProductInfo("HYU-BP001", "현대 소나타 전용 브레이크 패드", "현대 소나타 2.0L용 순정 브레이크 패드", 50000.00, "BRAKE_SYSTEM"));
                compatibleProducts.add(createProductInfo("HYU-OF003", "현대 소나타 전용 오일 필터", "현대 소나타 2.0L용 순정 오일 필터", 15000.00, "FILTERS"));
                compatibleProducts.add(createProductInfo("HYU-EO005", "현대 소나타 전용 엔진 오일", "현대 소나타 2.0L용 순정 엔진 오일", 11000.00, "FLUIDS_LUBRICANTS"));
            } else if (vc.getEngineType().contains("1.6L")) {
                compatibleProducts.add(createProductInfo("HYU-BP002", "현대 소나타 터보 전용 브레이크 패드", "현대 소나타 1.6L 터보용 순정 브레이크 패드", 55000.00, "BRAKE_SYSTEM"));
                compatibleProducts.add(createProductInfo("HYU-OF004", "현대 소나타 터보 전용 오일 필터", "현대 소나타 1.6L 터보용 순정 오일 필터", 16000.00, "FILTERS"));
                compatibleProducts.add(createProductInfo("HYU-EO006", "현대 소나타 터보 전용 엔진 오일", "현대 소나타 1.6L 터보용 순정 엔진 오일", 12000.00, "FLUIDS_LUBRICANTS"));
            }
        } else if (vc.getManufacturer().equals("현대") && vc.getModel().equals("아반떼")) {
            compatibleProducts.add(createProductInfo("HYU-BP003", "현대 아반떼 전용 브레이크 패드", "현대 아반떼 1.6L용 순정 브레이크 패드", 45000.00, "BRAKE_SYSTEM"));
            compatibleProducts.add(createProductInfo("HYU-OF005", "현대 아반떼 전용 오일 필터", "현대 아반떼 1.6L용 순정 오일 필터", 14000.00, "FILTERS"));
            compatibleProducts.add(createProductInfo("HYU-EO007", "현대 아반떼 전용 엔진 오일", "현대 아반떼 1.6L용 순정 엔진 오일", 10000.00, "FLUIDS_LUBRICANTS"));
        } else if (vc.getManufacturer().equals("기아") && vc.getModel().equals("K5")) {
            if (vc.getEngineType().contains("2.0L")) {
                compatibleProducts.add(createProductInfo("KIA-BP004", "기아 K5 전용 브레이크 패드", "기아 K5 2.0L용 순정 브레이크 패드", 52000.00, "BRAKE_SYSTEM"));
                compatibleProducts.add(createProductInfo("KIA-OF006", "기아 K5 전용 오일 필터", "기아 K5 2.0L용 순정 오일 필터", 15500.00, "FILTERS"));
                compatibleProducts.add(createProductInfo("KIA-EO008", "기아 K5 전용 엔진 오일", "기아 K5 2.0L용 순정 엔진 오일", 11500.00, "FLUIDS_LUBRICANTS"));
            } else if (vc.getEngineType().contains("1.6L")) {
                compatibleProducts.add(createProductInfo("KIA-BP005", "기아 K5 터보 전용 브레이크 패드", "기아 K5 1.6L 터보용 순정 브레이크 패드", 57000.00, "BRAKE_SYSTEM"));
                compatibleProducts.add(createProductInfo("KIA-OF007", "기아 K5 터보 전용 오일 필터", "기아 K5 1.6L 터보용 순정 오일 필터", 16500.00, "FILTERS"));
                compatibleProducts.add(createProductInfo("KIA-EO009", "기아 K5 터보 전용 엔진 오일", "기아 K5 1.6L 터보용 순정 엔진 오일", 12500.00, "FLUIDS_LUBRICANTS"));
            }
        } else if (vc.getManufacturer().equals("기아") && vc.getModel().equals("스포티지")) {
            if (vc.getEngineType().contains("2.0L")) {
                compatibleProducts.add(createProductInfo("KIA-BP006", "기아 스포티지 전용 브레이크 패드", "기아 스포티지 2.0L용 순정 브레이크 패드", 50000.00, "BRAKE_SYSTEM"));
                compatibleProducts.add(createProductInfo("KIA-OF008", "기아 스포티지 전용 오일 필터", "기아 스포티지 2.0L용 순정 오일 필터", 15000.00, "FILTERS"));
                compatibleProducts.add(createProductInfo("KIA-EO010", "기아 스포티지 전용 엔진 오일", "기아 스포티지 2.0L용 순정 엔진 오일", 11000.00, "FLUIDS_LUBRICANTS"));
            } else if (vc.getEngineType().contains("1.6L")) {
                compatibleProducts.add(createProductInfo("KIA-BP007", "기아 스포티지 터보 전용 브레이크 패드", "기아 스포티지 1.6L 터보용 순정 브레이크 패드", 55000.00, "BRAKE_SYSTEM"));
                compatibleProducts.add(createProductInfo("KIA-OF009", "기아 스포티지 터보 전용 오일 필터", "기아 스포티지 1.6L 터보용 순정 오일 필터", 16000.00, "FILTERS"));
                compatibleProducts.add(createProductInfo("KIA-EO011", "기아 스포티지 터보 전용 엔진 오일", "기아 스포티지 1.6L 터보용 순정 엔진 오일", 12000.00, "FLUIDS_LUBRICANTS"));
            }
        }

        return ResponseEntity.ok(compatibleProducts);
    }

    private Map<String, Object> createProductInfo(String sku, String name, String description, Double price, String partType) {
        Map<String, Object> product = new HashMap<>();
        product.put("sku", sku);
        product.put("name", name);
        product.put("description", description);
        product.put("price", price);
        product.put("partType", partType);
        product.put("manufacturer", "현대자동차");
        product.put("isOeQuality", true);
        product.put("isAftermarket", false);
        product.put("warrantyMonths", 12);
        product.put("stock", 100);
        return product;
    }
}