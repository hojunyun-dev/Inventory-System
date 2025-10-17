package com.inventory.repository;

import com.inventory.entity.SerializedInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerializedInventoryRepository extends JpaRepository<SerializedInventory, Long> {
    Optional<SerializedInventory> findBySerialNumber(String serialNumber);
    List<SerializedInventory> findByProductId(Long productId);
    List<SerializedInventory> findByStatus(String status);
    List<SerializedInventory> findByProductIdAndStatus(Long productId, String status);
}

