package com.demo.product.product_service.dto;

import java.math.BigDecimal;

import com.demo.product.product_service.model.Product;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Outgoing payload representing a Product.
 */
@Schema(description = "Product returned by the API")
public class ProductResponse {

	@Schema(description = "Generated product identifier", example = "1")
	private Long id;
	@Schema(description = "Product name", example = "Widget")
	private String name;
	@Schema(description = "Product description", example = "A simple widget")
	private String description;
	@Schema(description = "Unit price", example = "9.99")
	private BigDecimal price;

	public ProductResponse() {
	}

	public ProductResponse(Long id, String name, String description, BigDecimal price) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.price = price;
	}

	public static ProductResponse fromProduct(Product product) {
		return new ProductResponse(product.getId(), product.getName(), product.getDescription(), product.getPrice());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
