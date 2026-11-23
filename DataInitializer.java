package com.smartbus.config;

import com.smartbus.model.AppUser;
import com.smartbus.model.Bus;
import com.smartbus.repository.UserRepository;
import com.smartbus.repository.BusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class DataInitializer {
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CommandLineRunner initializeData(UserRepository users, BusRepository buses,
                                      BCryptPasswordEncoder encoder) {
        return args -> {
            Files.createDirectories(Path.of("data"));
            if (users.findByEmailIgnoreCase("admin@smartbus.com").isEmpty()) {
                AppUser admin = new AppUser();
                admin.name = "SmartBus Admin";
                admin.email = "admin@smartbus.com";
                admin.passwordHash = encoder.encode("Admin@123");
                admin.role = "ADMIN";
                users.save(admin);
            }
            if (buses.count() == 0) {
                add(buses, "SB101", "Himalayan Express", "Haldwani", "Delhi", "06:30", "13:00", 650, 40);
                add(buses, "SB102", "Doaba Travels", "Delhi", "Haldwani", "20:00", "03:30", 720, 36);
                add(buses, "SB103", "Royal Roadways", "Haldwani", "Dehradun", "08:00", "13:00", 450, 40);
                add(buses, "SB104", "City Link", "Agra", "Delhi", "07:15", "11:00", 380, 32);
                add(buses, "SB105", "North Star", "Jaipur", "Delhi", "09:00", "14:00", 520, 40);
                add(buses, "SB106", "Ganga Travels", "Dehradun", "Haridwar", "10:00", "11:30", 180, 30);
                add(buses, "SB107", "Shiv Shakti", "Mathura", "Agra", "12:00", "13:30", 150, 30);
                add(buses, "SB108", "Uttarakhand Express", "Delhi", "Dehradun", "21:00", "04:00", 690, 40);
                add(buses, "SB109", "Pink City Bus", "Jaipur", "Agra", "06:00", "10:30", 490, 36);
                add(buses, "SB110", "Kumaon Travels", "Haldwani", "Agra", "19:00", "05:00", 950, 40);
            }
        };
    }

    private void add(BusRepository repo, String number, String name, String from, String to,
                     String dep, String arr, double fare, int seats) {
        Bus b = new Bus();
        b.busNumber = number; b.name = name; b.source = from; b.destination = to;
        b.departureTime = dep; b.arrivalTime = arr; b.fare = fare; b.totalSeats = seats;
        repo.save(b);
    }
}
