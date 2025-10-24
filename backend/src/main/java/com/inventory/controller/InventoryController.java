package com.inventory.controller;

import com.inventory.dto.InventoryDto;
import com.inventory.entity.Inventory;
import com.inventory.entity.Product;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryController(InventoryRepository inventoryRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<InventoryDto>> getAllInventories() {
        List<InventoryDto> inventoryDtos = inventoryRepository.findAll()
                .stream()
                .map(InventoryDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(inventoryDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> getInventoryById(@PathVariable Long id) {
        return inventoryRepository.findById(id)
                .map(InventoryDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody InventoryRequest request) {
        Optional<Product> product = productRepository.findById(request.getProductId());
        if (product.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Inventory inventory = new Inventory();
        inventory.setProduct(product.get());
        inventory.setQuantity(request.getQuantity());
        inventory.setWarehouseLocation(request.getWarehouseLocation());
        
        Inventory savedInventory = inventoryRepository.save(inventory);
        return ResponseEntity.ok(savedInventory);
    }
    
    public static class InventoryRequest {
        private Long productId;
        private Integer quantity;
        private String warehouseLocation;
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public String getWarehouseLocation() { return warehouseLocation; }
        public void setWarehouseLocation(String warehouseLocation) { this.warehouseLocation = warehouseLocation; }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        if (!inventoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        inventory.setId(id);
        Inventory updatedInventory = inventoryRepository.save(inventory);
        return ResponseEntity.ok(updatedInventory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long id) {
        if (!inventoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        inventoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}