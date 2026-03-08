package com.vanopt.logistics.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "selected_shipments")
public class SelectedShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimized_output_id", nullable = false)
    private OptimizedOutput optimizedOutput;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int volume;

    @Column(nullable = false)
    private long revenue;

    protected SelectedShipment() {
    }

    public SelectedShipment(String name, int volume, long revenue) {
        this.name = name;
        this.volume = volume;
        this.revenue = revenue;
    }

    public Long getId() {
        return id;
    }

    public OptimizedOutput getOptimizedOutput() {
        return optimizedOutput;
    }

    public void setOptimizedOutput(OptimizedOutput optimizedOutput) {
        this.optimizedOutput = optimizedOutput;
    }

    public String getName() {
        return name;
    }

    public int getVolume() {
        return volume;
    }

    public long getRevenue() {
        return revenue;
    }
}
