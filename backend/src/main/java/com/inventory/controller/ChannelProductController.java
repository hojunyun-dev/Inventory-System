package com.inventory.controller;

import com.inventory.dto.ChannelProductDto;
import com.inventory.entity.ChannelProduct;
import com.inventory.entity.Product;
import com.inventory.repository.ChannelProductRepository;
import com.inventory.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/channel-products")
public class ChannelProductController {

    private final ChannelProductRepository channelProductRepository;
    private final ProductRepository productRepository;

    public ChannelProductController(ChannelProductRepository channelProductRepository, ProductRepository productRepository) {
        this.channelProductRepository = channelProductRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public ResponseEntity<List<ChannelProductDto>> getAllChannelProducts() {
        try {
            List<ChannelProductDto> channelProductDtos = channelProductRepository.findAll()
                    .stream()
                    .map(ChannelProductDto::fromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(channelProductDtos);
        } catch (Exception e) {
            System.err.println("Error in getAllChannelProducts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // 빈 리스트 반환
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChannelProductDto> getChannelProductById(@PathVariable Long id) {
        return channelProductRepository.findById(id)
                .map(ChannelProductDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ChannelProductDto> createChannelProduct(@RequestBody ChannelProductRequest request) {
        Optional<Product> product = productRepository.findById(request.getProductId());
        if (product.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ChannelProduct channelProduct = new ChannelProduct();
        channelProduct.setProduct(product.get());
        channelProduct.setChannel(request.getChannel());
        channelProduct.setChannelPrice(request.getChannelPrice());
        channelProduct.setAllocatedQuantity(request.getAllocatedQuantity());
        channelProduct.setSoldQuantity(0);
        channelProduct.setStatus(ChannelProduct.Status.ACTIVE);
        
        ChannelProduct savedChannelProduct = channelProductRepository.save(channelProduct);
        return ResponseEntity.ok(ChannelProductDto.fromEntity(savedChannelProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChannelProductDto> updateChannelProduct(@PathVariable Long id, @RequestBody ChannelProductRequest request) {
        Optional<ChannelProduct> existingChannelProduct = channelProductRepository.findById(id);
        if (existingChannelProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ChannelProduct channelProduct = existingChannelProduct.get();
        channelProduct.setChannelPrice(request.getChannelPrice());
        channelProduct.setAllocatedQuantity(request.getAllocatedQuantity());
        
        ChannelProduct updatedChannelProduct = channelProductRepository.save(channelProduct);
        return ResponseEntity.ok(ChannelProductDto.fromEntity(updatedChannelProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChannelProduct(@PathVariable Long id) {
        if (!channelProductRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        channelProductRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    
    public static class ChannelProductRequest {
        private Long productId;
        private String channel;
        private Double channelPrice;
        private Integer allocatedQuantity;
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public Double getChannelPrice() { return channelPrice; }
        public void setChannelPrice(Double channelPrice) { this.channelPrice = channelPrice; }
        public Integer getAllocatedQuantity() { return allocatedQuantity; }
        public void setAllocatedQuantity(Integer allocatedQuantity) { this.allocatedQuantity = allocatedQuantity; }
    }
}
