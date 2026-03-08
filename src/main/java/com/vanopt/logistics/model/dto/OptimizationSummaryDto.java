package com.vanopt.logistics.model.dto;

import java.time.Instant;
import java.util.UUID;

public record OptimizationSummaryDto(
        UUID requestId,
        int totalVolume,
        long totalRevenue,
        Instant createdAt
) {
}
