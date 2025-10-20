package com.inventory.repository;

import com.inventory.entity.ChannelProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelProductRepository extends JpaRepository<ChannelProduct, Long> {
    List<ChannelProduct> findByChannel(String channel);
    List<ChannelProduct> findByProductId(Long productId);
}
