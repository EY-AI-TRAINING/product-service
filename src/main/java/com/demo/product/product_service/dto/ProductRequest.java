package com.demo.product.product_service.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Incoming payload for creating or updating a Product.
 */
@Schema(description = "Payload for creating or updating a product")
public class ProductRequest {

	@NotBlank(message = "name is required")
	@Schema(description = "Product name", example = "Widget", requiredMode = Schema.RequiredMode.REQUIRED)
	private String name;

	@Schema(description = "Optional product description", example = "A simple widget")
	private String description;

	@NotNull(message = "price is required")
	@DecimalMin(value = "0.0", inclusive = true, message = "price must be >= 0")
	@Schema(description = "Unit price; must be greater than or equal to 0", example = "9.99",
			requiredMode = Schema.RequiredMode.REQUIRED)
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
