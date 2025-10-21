package com.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "channel_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
    
    @Column(name = "channel", nullable = false)
    private String channel;
    
    @Column(name = "channel_price")
    private Double channelPrice;
    
    @Column(name = "allocated_quantity")
    private Integer allocatedQuantity;
    
    @Column(name = "sold_quantity")
    private Integer soldQuantity = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status = Status.ACTIVE;
    
    @Column(name = "platform_product_id")
    private String platformProductId;
    
    @Column(name = "platform_url")
    private String platformUrl;
    
    public enum Status {
        DRAFT, ACTIVE, OUT_OF_STOCK, PAUSED, DELETED, SYNC_PENDING, SYNC_FAILED
    }
    
    public Integer getAvailableQuantity() {
        return allocatedQuantity - soldQuantity;
    }
}

