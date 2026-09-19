package com.demo.product.product_service.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.product.product_service.dto.ProductRequest;

import java.math.BigDecimal;

/**
 * End-to-end MockMvc tests covering CRUD behavior of the Product API.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void listReturnsEmptyOrExistingProducts() throws Exception {
		mockMvc.perform(get("/api/products"))
				.andExpect(status().isOk());
	}

	@Test
	void createReturnsCreatedWithLocationAndBody() throws Exception {
		ProductRequest request = new ProductRequest("Widget", "A simple widget", new BigDecimal("9.99"));

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(header().exists("Location"))
				.andExpect(jsonPath("$.id").exists())
				.andExpect(jsonPath("$.name").value("Widget"))
				.andExpect(jsonPath("$.description").value("A simple widget"))
				.andExpect(jsonPath("$.price").value(9.99));
	}

	@Test
	void createWithBlankNameReturnsBadRequest() throws Exception {
		ProductRequest request = new ProductRequest("", "No name", new BigDecimal("1.00"));

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void createWithNegativePriceReturnsBadRequest() throws Exception {
		ProductRequest request = new ProductRequest("Gadget", "Negative price", new BigDecimal("-5.00"));

		mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getByIdReturnsCreatedProduct() throws Exception {
		ProductRequest request = new ProductRequest("Gizmo", "A gizmo", new BigDecimal("19.99"));

		String responseBody = mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Long id = objectMapper.readTree(responseBody).get("id").asLong();

		mockMvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("Gizmo"));
	}

	@Test
	void getByIdReturnsNotFoundForMissingProduct() throws Exception {
		mockMvc.perform(get("/api/products/{id}", 999_999L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void updateModifiesExistingProduct() throws Exception {
		ProductRequest createRequest = new ProductRequest("Original", "Before update", new BigDecimal("5.00"));

		String responseBody = mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Long id = objectMapper.readTree(responseBody).get("id").asLong();

		ProductRequest updateRequest = new ProductRequest("Updated", "After update", new BigDecimal("15.00"));

		mockMvc.perform(put("/api/products/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(id))
				.andExpect(jsonPath("$.name").value("Updated"))
				.andExpect(jsonPath("$.description").value("After update"))
				.andExpect(jsonPath("$.price").value(15.00));
	}

	@Test
	void updateReturnsNotFoundForMissingProduct() throws Exception {
		ProductRequest updateRequest = new ProductRequest("Ghost", "Does not exist", new BigDecimal("1.00"));

		mockMvc.perform(put("/api/products/{id}", 999_999L)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(updateRequest)))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteRemovesProduct() throws Exception {
		ProductRequest createRequest = new ProductRequest("Disposable", "Will be deleted", new BigDecimal("2.50"));

		String responseBody = mockMvc.perform(post("/api/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(createRequest)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		Long id = objectMapper.readTree(responseBody).get("id").asLong();

		mockMvc.perform(delete("/api/products/{id}", id))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/products/{id}", id))
				.andExpect(status().isNotFound());
	}

	@Test
	void deleteReturnsNotFoundForMissingProduct() throws Exception {
		mockMvc.perform(delete("/api/products/{id}", 999_999L))
				.andExpect(status().isNotFound());
	}

	@Test
	void openApiDocsDescribeProductEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("Product Service API"))
				.andExpect(jsonPath("$.paths['/api/products']").exists())
				.andExpect(jsonPath("$.paths['/api/products/{id}']").exists());
	}

	@Test
	void swaggerUiIsAvailable() throws Exception {
		mockMvc.perform(get("/swagger-ui.html"))
				.andExpect(status().is3xxRedirection());
	}
}
