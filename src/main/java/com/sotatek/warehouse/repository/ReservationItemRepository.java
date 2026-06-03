package com.sotatek.warehouse.repository;

import com.sotatek.warehouse.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {
}
