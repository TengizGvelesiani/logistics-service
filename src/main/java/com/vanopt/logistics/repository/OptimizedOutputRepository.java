package com.vanopt.logistics.repository;

import com.vanopt.logistics.model.entity.OptimizedOutput;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OptimizedOutputRepository extends JpaRepository<OptimizedOutput, Long> {

    @Query("SELECT o FROM OptimizedOutput o LEFT JOIN FETCH o.selectedShipments WHERE o.requestId = :requestId")
    Optional<OptimizedOutput> findByRequestId(UUID requestId);

    Page<OptimizedOutput> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
