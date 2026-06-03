package com.sotatek.warehouse.repository;

import com.sotatek.warehouse.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findBySku(String sku);

    List<Inventory> findBySkuIn(List<String> skus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.sku IN :skus ORDER BY i.sku")
    List<Inventory> findBySkuInForUpdate(@Param("skus") List<String> skus);
}
