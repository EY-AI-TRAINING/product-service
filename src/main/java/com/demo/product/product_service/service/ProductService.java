package com.demo.product.product_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.demo.product.product_service.dto.ProductRequest;
import com.demo.product.product_service.dto.ProductResponse;
import com.demo.product.product_service.exception.ProductNotFoundException;
import com.demo.product.product_service.model.Product;
import com.demo.product.product_service.repository.ProductRepository;

/**
 * Business logic for Product CRUD operations, backed by the in-memory repository.
 */
@Service
public class ProductService {

	private final ProductRepository productRepository;

	public ProductService(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	public List<ProductResponse> findAll() {
		return productRepository.findAll().stream()
				.map(ProductResponse::fromProduct)
				.toList();
	}

	public ProductResponse findById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
		return ProductResponse.fromProduct(product);
	}

	public ProductResponse create(ProductRequest request) {
		Product product = new Product(null, request.getName(), request.getDescription(), request.getPrice());
		Product saved = productRepository.save(product);
		return ProductResponse.fromProduct(saved);
	}

	public ProductResponse update(Long id, ProductRequest request) {
		if (!productRepository.existsById(id)) {
			throw new ProductNotFoundException(id);
		}
		Product product = new Product(id, request.getName(), request.getDescription(), request.getPrice());
		Product saved = productRepository.save(product);
		return ProductResponse.fromProduct(saved);
	}

	public void delete(Long id) {
		if (!productRepository.existsById(id)) {
			throw new ProductNotFoundException(id);
		}
		productRepository.deleteById(id);
	}
}
