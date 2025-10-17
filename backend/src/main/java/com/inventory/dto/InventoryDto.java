package com.inventory.dto;

import com.inventory.entity.Inventory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDto {
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productCategory;
    private Integer quantity;
    private String warehouseLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static InventoryDto fromEntity(Inventory inventory) {
        InventoryDto dto = new InventoryDto();
        dto.setId(inventory.getId());
        dto.setQuantity(inventory.getQuantity());
        dto.setWarehouseLocation(inventory.getWarehouseLocation());
        dto.setCreatedAt(inventory.getCreatedAt());
        dto.setUpdatedAt(inventory.getUpdatedAt());
        
        if (inventory.getProduct() != null) {
            dto.setProductId(inventory.getProduct().getId());
            dto.setProductName(inventory.getProduct().getName());
            dto.setProductSku(inventory.getProduct().getSku());
            if (inventory.getProduct().getCategory() != null) {
                dto.setProductCategory(inventory.getProduct().getCategory().getName());
            }
        }
        
        return dto;
    }
}
