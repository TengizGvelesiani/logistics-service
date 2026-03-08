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
@Table(name = "optimized_outputs")
public class OptimizedOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "request_id", nullable = false, unique = true)
    private UUID requestId;

    @Column(name = "total_volume", nullable = false)
    private int totalVolume;

    @Column(name = "total_revenue", nullable = false)
    private long totalRevenue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "optimizedOutput", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SelectedShipment> selectedShipments = new ArrayList<>();

    protected OptimizedOutput() {
    }

    public OptimizedOutput(UUID requestId, int totalVolume, long totalRevenue, Instant createdAt) {
        this.requestId = requestId;
        this.totalVolume = totalVolume;
        this.totalRevenue = totalRevenue;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public int getTotalVolume() {
        return totalVolume;
    }

    public long getTotalRevenue() {
        return totalRevenue;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<SelectedShipment> getSelectedShipments() {
        return selectedShipments;
    }

    public void addSelectedShipment(SelectedShipment shipment) {
        selectedShipments.add(shipment);
        shipment.setOptimizedOutput(this);
    }
}
