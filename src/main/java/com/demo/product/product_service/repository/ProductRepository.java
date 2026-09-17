package com.demo.product.product_service.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

import com.demo.product.product_service.model.Product;

/**
 * Thread-safe in-memory store for Product entities.
 */
@Repository
public class ProductRepository {

	private final Map<Long, Product> store = new ConcurrentHashMap<>();
	private final AtomicLong idGenerator = new AtomicLong(0);

	public List<Product> findAll() {
		return new ArrayList<>(store.values());
	}

	public Optional<Product> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	public boolean existsById(Long id) {
		return store.containsKey(id);
	}

	public Product save(Product product) {
		if (product.getId() == null) {
			product.setId(idGenerator.incrementAndGet());
		}
		store.put(product.getId(), product);
		return product;
	}

	public void deleteById(Long id) {
		store.remove(id);
	}
}
