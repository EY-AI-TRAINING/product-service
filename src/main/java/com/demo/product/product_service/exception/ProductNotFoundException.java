package com.demo.product.product_service.exception;

/**
 * Thrown when a requested Product cannot be found in the repository.
 */
public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("Product not found with id: " + id);
	}
}
