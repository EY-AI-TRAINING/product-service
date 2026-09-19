package com.demo.product.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

/**
 * OpenAPI / Swagger metadata for the Product Service.
 */
@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI productServiceOpenAPI() {
		return new OpenAPI().info(new Info()
				.title("Product Service API")
				.description("REST API for creating, reading, updating, and deleting products.")
				.version("v1"));
	}
}
