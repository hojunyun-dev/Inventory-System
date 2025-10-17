package com.inventory.controller;

import com.inventory.entity.SerializedInventory;
import com.inventory.repository.SerializedInventoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/serialized-inventories")
public class SerializedInventoryController {

    private final SerializedInventoryRepository serializedInventoryRepository;

    public SerializedInventoryController(SerializedInventoryRepository serializedInventoryRepository) {
        this.serializedInventoryRepository = serializedInventoryRepository;
    }

    @GetMapping
    public ResponseEntity<List<SerializedInventory>> getAllSerializedInventories() {
        return ResponseEntity.ok(serializedInventoryRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerializedInventory> getSerializedInventoryById(@PathVariable Long id) {
        return serializedInventoryRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SerializedInventory> createSerializedInventory(@RequestBody SerializedInventory serializedInventory) {
        SerializedInventory savedSerializedInventory = serializedInventoryRepository.save(serializedInventory);
        return ResponseEntity.ok(savedSerializedInventory);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SerializedInventory> updateSerializedInventory(@PathVariable Long id, @RequestBody SerializedInventory serializedInventory) {
        if (!serializedInventoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serializedInventory.setId(id);
        SerializedInventory updatedSerializedInventory = serializedInventoryRepository.save(serializedInventory);
        return ResponseEntity.ok(updatedSerializedInventory);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSerializedInventory(@PathVariable Long id) {
        if (!serializedInventoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        serializedInventoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

