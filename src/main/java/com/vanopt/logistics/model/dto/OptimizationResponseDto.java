package com.vanopt.logistics.model.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OptimizationResponseDto(
        UUID requestId,
        List<ShipmentDTO> selectedShipments,
        int totalVolume,
        long totalRevenue,
        Instant createdAt
) {
}
