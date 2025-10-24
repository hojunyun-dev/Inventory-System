package com.inventory.dto;

import com.inventory.entity.ChannelProduct;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelProductDto {
    
    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productOemNumber;
    private String channel;
    private Double channelPrice;
    private Integer allocatedQuantity;
    private Integer soldQuantity;
    private Integer availableQuantity;
    private ChannelProduct.Status status;
    private String platformProductId;
    private String platformUrl;
    
    public static ChannelProductDto fromEntity(ChannelProduct entity) {
        ChannelProductDto dto = new ChannelProductDto();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProduct().getId());
        dto.setProductName(entity.getProduct().getName());
        dto.setProductSku(entity.getProduct().getSku());
        dto.setProductOemNumber(entity.getProduct().getOemPartNumber());
        dto.setChannel(entity.getChannel());
        dto.setChannelPrice(entity.getChannelPrice());
        dto.setAllocatedQuantity(entity.getAllocatedQuantity());
        dto.setSoldQuantity(entity.getSoldQuantity());
        dto.setAvailableQuantity(entity.getAvailableQuantity());
        dto.setStatus(entity.getStatus());
        dto.setPlatformProductId(entity.getPlatformProductId());
        dto.setPlatformUrl(entity.getPlatformUrl());
        return dto;
    }
}
