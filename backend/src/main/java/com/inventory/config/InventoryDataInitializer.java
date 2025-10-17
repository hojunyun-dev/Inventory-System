package com.inventory.config;

import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class InventoryDataInitializer implements CommandLineRunner {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        // 기존 재고 데이터가 없으면 생성
        if (inventoryRepository.count() == 0) {
            createInventoryData();
        }
    }

    private void createInventoryData() {
        var products = productRepository.findAll();
        
        for (Product product : products) {
            Inventory inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(generateRandomQuantity(product.getName()));
            inventory.setWarehouseLocation(generateWarehouseLocation());
            inventory.setCreatedAt(LocalDateTime.now());
            inventory.setUpdatedAt(LocalDateTime.now());
            
            inventoryRepository.save(inventory);
        }
    }
    
    private Integer generateRandomQuantity(String productName) {
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

