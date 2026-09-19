package com.demo.product.product_service.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Query payload for searching products by text.
 */
@Schema(description = "Search term for finding products by name or description")
public class ProductSearchRequest {

	@NotBlank(message = "q is required")
	@Schema(description = "Non-blank search term; matched in name or description", example = "widget",
			requiredMode = Schema.RequiredMode.REQUIRED)
	private String q;

	public ProductSearchRequest() {
	}

	public ProductSearchRequest(String q) {
		this.q = q;
	}

	public String getQ() {
		return q;
	}

	public void setQ(String q) {
		this.q = q;
	}
}
