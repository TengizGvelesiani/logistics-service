package com.vanopt.logistics.controller;

import com.vanopt.logistics.model.dto.OptimizationResponseDto;
import com.vanopt.logistics.model.dto.OptimizationSummaryDto;
import com.vanopt.logistics.model.dto.OptimizeRequestDto;
import com.vanopt.logistics.service.OptimizationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class OptimizationController {

    private final OptimizationService optimizationService;

    public OptimizationController(OptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @PostMapping(value = "/optimize", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OptimizationResponseDto optimize(@Valid @RequestBody OptimizeRequestDto request) {
        return optimizationService.optimize(
                request.maxVolume(),
                request.availableShipments()
        );
    }

    @GetMapping(value = "/optimizations/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public OptimizationResponseDto getByRequestId(@PathVariable UUID requestId) {
        return optimizationService.getByRequestId(requestId);
    }

    @GetMapping(value = "/optimizations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<OptimizationSummaryDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return optimizationService.getAll(pageable);
    }
}
