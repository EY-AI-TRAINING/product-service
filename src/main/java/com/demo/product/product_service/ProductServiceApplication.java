package com.demo.product.product_service;

import java.time.LocalDateTime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		System.out.println("Hello World");
		// print the current date and time
		System.out.println(LocalDateTime.now());
		SpringApplication.run(ProductServiceApplication.class, args);
	}

}
