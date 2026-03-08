package com.vanopt.logistics.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ShipmentRequest(
        @NotBlank(message = "Shipment name must not be blank")
        String name,

        @NotNull(message = "Volume is required")
        @Positive(message = "Volume must be positive")
        Integer volume,

        @NotNull(message = "Revenue is required")
        @PositiveOrZero(message = "Revenue must be zero or positive")
        Long revenue
) {
}
