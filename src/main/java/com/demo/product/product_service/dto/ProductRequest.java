package com.demo.product.product_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Incoming payload for creating or updating a Product.
 */
public class ProductRequest {

	@NotBlank(message = "name is required")
	private String name;

	private String description;

	@NotNull(message = "price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "price must be >= 0")
	private BigDecimal price;

	public ProductRequest() {
	}

	public ProductRequest(String name, String description, BigDecimal price) {
		this.name = name;
		this.description = description;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
}
