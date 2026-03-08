package com.vanopt.logistics.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "optimization_inputs")
public class OptimizationInput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    @Column(name = "max_volume", nullable = false)
    private int maxVolume;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "optimizationInput", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InputShipment> inputShipments = new ArrayList<>();

    protected OptimizationInput() {
    }

    public OptimizationInput(UUID requestId, int maxVolume, Instant createdAt) {
        this.requestId = requestId;
        this.maxVolume = maxVolume;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public int getMaxVolume() {
        return maxVolume;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<InputShipment> getInputShipments() {
        return inputShipments;
    }

    public void addInputShipment(InputShipment shipment) {
        inputShipments.add(shipment);
        shipment.setOptimizationInput(this);
    }
}
