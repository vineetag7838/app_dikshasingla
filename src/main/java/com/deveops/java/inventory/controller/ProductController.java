package com.deveops.java.inventory.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.deveops.java.inventory.dto.InventoryBEObjectBaseResponse;
import com.deveops.java.inventory.model.Product;
import com.deveops.java.inventory.service.ProductService;

@RestController
@RequestMapping(value = "/inventory/")
public class ProductController {

	@Autowired
	private ProductService productService;

	@PostMapping("add")
	public InventoryBEObjectBaseResponse addProduct(@RequestBody Product product) {
		return productService.addProduct(product);
	}

	@GetMapping("getAllProducts")
	public InventoryBEObjectBaseResponse getAllProducts() {
		return productService.getAllProducts();
	}

	@GetMapping("getProductById")
	private InventoryBEObjectBaseResponse getProductById(@RequestParam(value = "productId") String productId) {
		return productService.getProductById(productId);
	}

}
