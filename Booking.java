package com.smartbus.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, unique = true)
    public String pnr;
    public Long userId;
    public Long busId;
    public String busName;
    public String busNumber;
    public String source;
    public String destination;
    public String passengerName;
    public int age;
    public String gender;
    public String mobile;
    public String travelDate;
    public int seatNumber;
    public double fare;
    public String paymentMode;
    public String paymentStatus;
    public String status = "CONFIRMED";
    public LocalDateTime createdAt = LocalDateTime.now();

    public Booking() {}
}
