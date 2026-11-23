package com.smartbus.model;

import jakarta.persistence.*;

@Entity
@Table(name = "buses")
public class Bus {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String busNumber;
    @Column(nullable = false)
    public String name;
    @Column(nullable = false)
    public String source;
    @Column(nullable = false)
    public String destination;
    @Column(nullable = false)
    public String departureTime;
    @Column(nullable = false)
    public String arrivalTime;
    public double fare;
    public int totalSeats = 40;
    public boolean active = true;

    public Bus() {}
}
