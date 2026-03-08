package com.vanopt.logistics.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record OptimizeRequestDto(
        @NotNull(message = "maxVolume is required")
        @Positive(message = "maxVolume must be positive")
        Integer maxVolume,

        @NotNull(message = "availableShipments is required")
        @NotEmpty(message = "availableShipments must not be empty")
        @Valid
        List<ShipmentRequest> availableShipments
) {
}
