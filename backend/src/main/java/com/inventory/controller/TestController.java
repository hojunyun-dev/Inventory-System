package com.inventory.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/channel-products")
    public List<String> getTestChannelProducts() {
        return List.of("Test Channel Product 1", "Test Channel Product 2");
    }
}
