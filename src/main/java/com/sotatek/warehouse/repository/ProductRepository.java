package com.sotatek.warehouse.repository;

import com.sotatek.warehouse.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
