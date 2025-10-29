package com.inventory.controller;

import com.inventory.entity.Category;
import com.inventory.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryRepository categoryRepository;

	@GetMapping
	public ResponseEntity<List<Category>> getCategories() {
		List<Category> categories = categoryRepository.findByIsActiveTrue();
		return ResponseEntity.ok(categories);
	}

	@GetMapping("/root")
	public ResponseEntity<List<Category>> getRootCategories() {
		List<Category> rootCategories = categoryRepository.findByParentIsNullAndIsActiveTrue();
		return ResponseEntity.ok(rootCategories);
	}

	@GetMapping("/{parentId}/children")
	public ResponseEntity<List<Category>> getCategoryChildren(@PathVariable("parentId") Long parentId) {
		List<Category> children = categoryRepository.findChildrenByParentId(parentId);
		return ResponseEntity.ok(children);
	}
}
