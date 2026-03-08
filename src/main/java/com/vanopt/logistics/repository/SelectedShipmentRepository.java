package com.vanopt.logistics.repository;

import com.vanopt.logistics.model.entity.SelectedShipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SelectedShipmentRepository extends JpaRepository<SelectedShipment, Long> {
}
