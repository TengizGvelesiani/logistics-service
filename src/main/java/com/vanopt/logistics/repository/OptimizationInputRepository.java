package com.vanopt.logistics.repository;

import com.vanopt.logistics.model.entity.OptimizationInput;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface OptimizationInputRepository extends JpaRepository<OptimizationInput, Long> {

    Optional<OptimizationInput> findByRequestId(UUID requestId);

    @Modifying
    @Query("DELETE FROM OptimizationInput o WHERE o.createdAt < :before")
    int deleteByCreatedAtBefore(Instant before);
}
