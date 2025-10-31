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

    /**
     * 등록서비스(8082)에서 성공적으로 플랫폼에 상품을 등록한 뒤 호출하는 콜백 엔드포인트.
     * - 멱등: productId + channel 조합으로 기존 레코드가 있으면 갱신, 없으면 생성
     * - platformProductId / platformUrl 반영, 상태는 ACTIVE로 세팅
     */
    @PostMapping("/callback")
    public ResponseEntity<ChannelProductDto> upsertFromRegistration(@RequestBody RegistrationCallbackRequest request) {
        if (request.getProductId() == null || request.getChannel() == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<Product> productOpt = productRepository.findById(request.getProductId());
        if (productOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // find existing by productId then filter by channel (간단 구현)
        List<ChannelProduct> existingList = channelProductRepository.findByProductId(request.getProductId());
        ChannelProduct entity = existingList.stream()
                .filter(cp -> request.getChannel().equalsIgnoreCase(cp.getChannel()))
                .findFirst()
                .orElseGet(() -> {
                    ChannelProduct cp = new ChannelProduct();
                    cp.setProduct(productOpt.get());
                    cp.setChannel(request.getChannel());
                    cp.setStatus(ChannelProduct.Status.ACTIVE);
                    cp.setAllocatedQuantity(0);
                    cp.setSoldQuantity(0);
                    return cp;
                });

        entity.setPlatformProductId(request.getPlatformProductId());
        entity.setPlatformUrl(request.getPlatformUrl());
        entity.setStatus(ChannelProduct.Status.ACTIVE);

        ChannelProduct saved = channelProductRepository.save(entity);
        return ResponseEntity.ok(ChannelProductDto.fromEntity(saved));
    }

    public static class RegistrationCallbackRequest {
        private Long productId;
        private String channel; // BUNJANG 등
        private String platformProductId;
        private String platformUrl;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        public String getPlatformProductId() { return platformProductId; }
        public void setPlatformProductId(String platformProductId) { this.platformProductId = platformProductId; }
        public String getPlatformUrl() { return platformUrl; }
        public void setPlatformUrl(String platformUrl) { this.platformUrl = platformUrl; }
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
