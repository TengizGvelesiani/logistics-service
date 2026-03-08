package com.vanopt.logistics.service;

import com.vanopt.logistics.exception.ResourceNotFoundException;
import com.vanopt.logistics.model.dto.OptimizationResponseDto;
import com.vanopt.logistics.model.dto.OptimizationSummaryDto;
import com.vanopt.logistics.model.dto.ShipmentRequest;
import com.vanopt.logistics.model.entity.OptimizedOutput;
import com.vanopt.logistics.model.entity.SelectedShipment;
import com.vanopt.logistics.repository.OptimizationInputRepository;
import com.vanopt.logistics.repository.OptimizedOutputRepository;
import com.vanopt.logistics.algorithm.KnapsackAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OptimizationServiceTest {

    @Mock
    private OptimizedOutputRepository optimizedOutputRepository;

    @Mock
    private OptimizationInputRepository optimizationInputRepository;

    @Mock
    private KnapsackAlgorithm knapsackAlgorithm;

    @InjectMocks
    private OptimizationService optimizationService;

    @Test
    void optimize_persistsInputAndOutput_returnsResponse() {
        List<ShipmentRequest> shipments = List.of(
                new ShipmentRequest("A", 5, 120L),
                new ShipmentRequest("B", 10, 200L)
        );
        when(knapsackAlgorithm.solve(eq(15), any())).thenReturn(List.of(0, 1));
        when(optimizedOutputRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(optimizationInputRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OptimizationResponseDto result = optimizationService.optimize(15, shipments);

        assertThat(result.requestId()).isNotNull();
        assertThat(result.selectedShipments()).hasSize(2);
        assertThat(result.totalVolume()).isEqualTo(15);
        assertThat(result.totalRevenue()).isEqualTo(320);
        verify(optimizationInputRepository).save(any());
        verify(optimizedOutputRepository).save(any());
    }

    @Test
    void getByRequestId_returnsResponse() {
        UUID requestId = UUID.randomUUID();
        OptimizedOutput entity = new OptimizedOutput(requestId, 15, 320, Instant.now());
        entity.addSelectedShipment(new SelectedShipment("A", 5, 120));
        entity.addSelectedShipment(new SelectedShipment("B", 10, 200));
        when(optimizedOutputRepository.findByRequestId(requestId)).thenReturn(Optional.of(entity));

        OptimizationResponseDto result = optimizationService.getByRequestId(requestId);

        assertThat(result.requestId()).isEqualTo(requestId);
        assertThat(result.selectedShipments()).hasSize(2);
        assertThat(result.totalVolume()).isEqualTo(15);
        assertThat(result.totalRevenue()).isEqualTo(320);
    }

    @Test
    void getByRequestId_notFound_throws() {
        UUID requestId = UUID.randomUUID();
        when(optimizedOutputRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> optimizationService.getByRequestId(requestId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(requestId.toString());
    }

    @Test
    void getAll_returnsPage() {
        OptimizedOutput r = new OptimizedOutput(UUID.randomUUID(), 10, 100, Instant.now());
        when(optimizedOutputRepository.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r), PageRequest.of(0, 20), 1));

        var page = optimizationService.getAll(PageRequest.of(0, 20));

        assertThat(page.getContent()).hasSize(1);
        OptimizationSummaryDto dto = page.getContent().getFirst();
        assertThat(dto.totalVolume()).isEqualTo(10);
        assertThat(dto.totalRevenue()).isEqualTo(100);
    }
}
