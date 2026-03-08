package com.vanopt.logistics.service;

import com.vanopt.logistics.algorithm.KnapsackAlgorithm;
import com.vanopt.logistics.algorithm.KnapsackItem;
import com.vanopt.logistics.exception.ResourceNotFoundException;
import com.vanopt.logistics.model.dto.OptimizationResponseDto;
import com.vanopt.logistics.model.dto.OptimizationSummaryDto;
import com.vanopt.logistics.model.dto.ShipmentDTO;
import com.vanopt.logistics.model.dto.ShipmentRequest;
import com.vanopt.logistics.model.entity.InputShipment;
import com.vanopt.logistics.model.entity.OptimizationInput;
import com.vanopt.logistics.model.entity.OptimizedOutput;
import com.vanopt.logistics.model.entity.SelectedShipment;
import com.vanopt.logistics.repository.OptimizationInputRepository;
import com.vanopt.logistics.repository.OptimizedOutputRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OptimizationService {

    private final OptimizedOutputRepository optimizedOutputRepository;
    private final OptimizationInputRepository optimizationInputRepository;
    private final KnapsackAlgorithm knapsackAlgorithm;

    public OptimizationService(OptimizedOutputRepository optimizedOutputRepository,
                               OptimizationInputRepository optimizationInputRepository,
                               KnapsackAlgorithm knapsackAlgorithm) {
        this.optimizedOutputRepository = optimizedOutputRepository;
        this.optimizationInputRepository = optimizationInputRepository;
        this.knapsackAlgorithm = knapsackAlgorithm;
    }

    @Transactional
    public OptimizationResponseDto optimize(int maxVolume, List<ShipmentRequest> availableShipments) {
        UUID requestId = UUID.randomUUID();
        Instant now = Instant.now();

        OptimizationInput input = new OptimizationInput(requestId, maxVolume, now);
        for (ShipmentRequest s : availableShipments) {
            input.addInputShipment(new InputShipment(s.name(), s.volume(), s.revenue()));
        }
        optimizationInputRepository.save(input);

        List<KnapsackItem> items = availableShipments.stream()
                .map(s -> new KnapsackItem(s.volume(), s.revenue()))
                .toList();
        List<Integer> selectedIndices = knapsackAlgorithm.solve(maxVolume, items);

        List<ShipmentDTO> selected = new ArrayList<>();
        int totalVolume = 0;
        long totalRevenue = 0;
        for (int idx : selectedIndices) {
            ShipmentRequest s = availableShipments.get(idx);
            selected.add(new ShipmentDTO(s.name(), s.volume(), s.revenue()));
            totalVolume += s.volume();
            totalRevenue += s.revenue();
        }

        OptimizedOutput result = new OptimizedOutput(requestId, totalVolume, totalRevenue, now);
        for (int idx : selectedIndices) {
            ShipmentRequest s = availableShipments.get(idx);
            result.addSelectedShipment(new SelectedShipment(s.name(), s.volume(), s.revenue()));
        }
        optimizedOutputRepository.save(result);

        return new OptimizationResponseDto(requestId, selected, totalVolume, totalRevenue, now);
    }

    @Transactional(readOnly = true)
    public OptimizationResponseDto getByRequestId(UUID requestId) {
        OptimizedOutput output = optimizedOutputRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Optimization not found for requestId: " + requestId));
        List<ShipmentDTO> shipments = output.getSelectedShipments().stream()
                .map(s -> new ShipmentDTO(s.getName(), s.getVolume(), s.getRevenue()))
                .toList();
        return new OptimizationResponseDto(
                output.getRequestId(),
                shipments,
                output.getTotalVolume(),
                output.getTotalRevenue(),
                output.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Page<OptimizationSummaryDto> getAll(Pageable pageable) {
        return optimizedOutputRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(r -> new OptimizationSummaryDto(
                        r.getRequestId(),
                        r.getTotalVolume(),
                        r.getTotalRevenue(),
                        r.getCreatedAt()
                ));
    }

    @Transactional
    public void deleteInputOlderThan(int days) {
        Instant before = Instant.now().minus(days, ChronoUnit.DAYS);
        optimizationInputRepository.deleteByCreatedAtBefore(before);
    }
}
