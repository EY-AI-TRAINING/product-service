package com.demo.product.product_service.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;

import com.demo.product.product_service.dto.ProductRequest;
import com.demo.product.product_service.dto.ProductResponse;
import com.demo.product.product_service.dto.ProductSearchRequest;
import com.demo.product.product_service.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST endpoints for Product CRUD operations and catalog search.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Create, read, update, delete, and search products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping
	@Operation(summary = "List products", description = "Returns every product currently stored in the catalog.")
	@ApiResponse(responseCode = "200", description = "Products retrieved",
			content = @Content(mediaType = "application/json",
					array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
	public List<ProductResponse> getAll() {
		return productService.findAll();
	}

	@GetMapping("/search")
	@Operation(summary = "Search products",
			description = "Requires a non-blank query parameter q. Returns products whose name or "
					+ "description contains q (case-insensitive). An empty array means no matches, not a missing catalog.")
	@ApiResponses({
			@ApiResponse(responseCode = "200",
					description = "Search succeeded. Body is the matching products, or an empty array when nothing matches.",
					content = @Content(mediaType = "application/json",
							array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)))),
			@ApiResponse(responseCode = "400",
					description = "Invalid input: q is missing, blank, or only whitespace.",
					content = @Content(mediaType = "application/json"))
	})
	public List<ProductResponse> search(@Valid ProductSearchRequest request) {
		return productService.search(request.getQ());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get product by id", description = "Returns a single product when it exists.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Product found",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "404", description = "Product not found",
					content = @Content(mediaType = "application/json"))
	})
	public ProductResponse getById(
			@Parameter(description = "Product identifier", example = "1", required = true)
			@PathVariable Long id) {
		return productService.findById(id);
	}

	@PostMapping
	@Operation(summary = "Create product", description = "Creates a product and returns it with a generated id.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "Product created",
					headers = @Header(name = "Location", description = "URI of the created product"),
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "Validation failed",
					content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
		ProductResponse created = productService.create(request);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.getId())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update product", description = "Replaces an existing product's name, description, and price.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Product updated",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "Validation failed",
					content = @Content(mediaType = "application/json")),
			@ApiResponse(responseCode = "404", description = "Product not found",
					content = @Content(mediaType = "application/json"))
	})
	public ProductResponse update(
			@Parameter(description = "Product identifier", example = "1", required = true)
			@PathVariable Long id,
			@Valid @RequestBody ProductRequest request) {
		return productService.update(id, request);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete product", description = "Removes a product from the catalog.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Product deleted", content = @Content),
			@ApiResponse(responseCode = "404", description = "Product not found",
					content = @Content(mediaType = "application/json"))
	})
	public ResponseEntity<Void> delete(
			@Parameter(description = "Product identifier", example = "1", required = true)
			@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}
}
