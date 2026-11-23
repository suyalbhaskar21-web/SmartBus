package com.smartbus.controller;

import com.smartbus.model.*;
import com.smartbus.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final UserRepository users;
    private final BusRepository buses;
    private final BookingRepository bookings;
    private final BCryptPasswordEncoder encoder;

    public ApiController(UserRepository users, BusRepository buses, BookingRepository bookings,
                         BCryptPasswordEncoder encoder) {
        this.users = users; this.buses = buses; this.bookings = bookings; this.encoder = encoder;
    }

    private AppUser current(HttpSession session) {
        Object id = session.getAttribute("userId");
        return id == null ? null : users.findById((Long) id).orElse(null);
    }
    private Map<String,Object> error(String message) {
        return Map.of("message", message);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String,String> body, HttpSession session) {
        String name = body.getOrDefault("name", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        if (name.isBlank() || email.isBlank() || password.length() < 6)
            return ResponseEntity.badRequest().body(error("Name, email and a password of at least 6 characters are required."));
        if (users.findByEmailIgnoreCase(email).isPresent())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error("This email is already registered."));
        AppUser u = new AppUser(); u.name = name; u.email = email;
        u.passwordHash = encoder.encode(password); u.role = "USER"; users.save(u);
        session.setAttribute("userId", u.id);
        return ResponseEntity.ok(userInfo(u));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,String> body, HttpSession session) {
        String email = body.getOrDefault("email", "").trim();
        String password = body.getOrDefault("password", "");
        AppUser u = users.findByEmailIgnoreCase(email).orElse(null);
        if (u == null || !encoder.matches(password, u.passwordHash))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("Incorrect email or password."));
        session.setAttribute("userId", u.id);
        return ResponseEntity.ok(userInfo(u));
    }

    @PostMapping("/auth/logout")
    public Map<String,String> logout(HttpSession session) {
        session.invalidate(); return Map.of("message", "Logged out");
    }

    @GetMapping("/auth/me")
    public ResponseEntity<?> me(HttpSession session) {
        AppUser u = current(session);
        return u == null ? ResponseEntity.status(401).body(error("Please log in.")) : ResponseEntity.ok(userInfo(u));
    }

    private Map<String,Object> userInfo(AppUser u) {
        return Map.of("id", u.id, "name", u.name, "email", u.email, "role", u.role);
    }

    @GetMapping("/buses")
    public List<Bus> searchBuses(@RequestParam(required=false) String source,
                                 @RequestParam(required=false) String destination) {
        if (source != null && destination != null && !source.isBlank() && !destination.isBlank())
            return buses.findByActiveTrueAndSourceIgnoreCaseAndDestinationIgnoreCase(source.trim(), destination.trim());
        return buses.findByActiveTrueOrderByDepartureTimeAsc();
    }

    @GetMapping("/buses/{id}/seats")
    public ResponseEntity<?> seats(@PathVariable Long id, @RequestParam String date) {
        Bus bus = buses.findById(id).orElse(null);
        if (bus == null) return ResponseEntity.notFound().build();
        Set<Integer> occupied = new HashSet<>();
        for (Booking b : bookings.findAll()) {
            if (Objects.equals(b.busId, id) && Objects.equals(b.travelDate, date) && "CONFIRMED".equals(b.status))
                occupied.add(b.seatNumber);
        }
        List<Map<String,Object>> result = new ArrayList<>();
        for (int i=1; i<=bus.totalSeats; i++)
            result.add(Map.of("number", i, "available", !occupied.contains(i)));
        return ResponseEntity.ok(result);
    }

    @PostMapping("/bookings")
    @Transactional
    public ResponseEntity<?> book(@RequestBody Map<String,Object> body, HttpSession session) {
        AppUser user = current(session);
        if (user == null) return ResponseEntity.status(401).body(error("Please log in before booking."));
        try {
            Long busId = Long.valueOf(body.get("busId").toString());
            int seat = Integer.parseInt(body.get("seatNumber").toString());
            String date = body.get("travelDate").toString();
            Bus bus = buses.findById(busId).orElse(null);
            if (bus == null || !bus.active) return ResponseEntity.badRequest().body(error("Bus not found."));
            if (seat < 1 || seat > bus.totalSeats) return ResponseEntity.badRequest().body(error("Invalid seat number."));
            if (bookings.existsByBusIdAndTravelDateAndSeatNumberAndStatus(busId, date, seat, "CONFIRMED"))
                return ResponseEntity.status(409).body(error("This seat has already been booked. Please choose another."));
            String passenger = Objects.toString(body.get("passengerName"), "").trim();
            String mobile = Objects.toString(body.get("mobile"), "").trim();
            int age = Integer.parseInt(body.get("age").toString());
            if (passenger.isBlank() || !mobile.matches("[0-9]{10}") || age < 1 || age > 100)
                return ResponseEntity.badRequest().body(error("Enter a passenger name, valid 10-digit mobile and age."));
            if (date.compareTo(java.time.LocalDate.now().toString()) < 0)
                return ResponseEntity.badRequest().body(error("Travel date cannot be in the past."));
            Booking b = new Booking();
            b.pnr = "SB" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10,99);
            b.userId = user.id; b.busId = bus.id; b.busName = bus.name; b.busNumber = bus.busNumber;
            b.source = bus.source; b.destination = bus.destination;
            b.passengerName = passenger; b.age = age;
            b.gender = Objects.toString(body.get("gender"), "Not specified");
            b.mobile = mobile; b.travelDate = date; b.seatNumber = seat;
            b.fare = bus.fare; b.paymentMode = Objects.toString(body.get("paymentMode"), "PAY_AT_BOARDING");
            b.paymentStatus = "PAY_AT_BOARDING".equals(b.paymentMode) ? "PAY AT BOARDING" : "DEMO PAID";
            b.status = "CONFIRMED";
            bookings.save(b);
            return ResponseEntity.ok(b);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(error("Please check all booking details and try again."));
        }
    }

    @GetMapping("/bookings/mine")
    public ResponseEntity<?> mine(HttpSession session) {
        AppUser user = current(session);
        if (user == null) return ResponseEntity.status(401).body(error("Please log in."));
        return ResponseEntity.ok(bookings.findByUserIdOrderByCreatedAtDesc(user.id));
    }

    @GetMapping("/bookings/pnr/{pnr}")
    public ResponseEntity<?> byPnr(@PathVariable String pnr, HttpSession session) {
        AppUser user = current(session);
        if (user == null) return ResponseEntity.status(401).body(error("Please log in."));
        Booking b = bookings.findByPnrIgnoreCase(pnr).orElse(null);
        if (b == null || (!"ADMIN".equals(user.role) && !Objects.equals(b.userId, user.id)))
            return ResponseEntity.status(404).body(error("Booking not found."));
        return ResponseEntity.ok(b);
    }

    @PostMapping("/bookings/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id, HttpSession session) {
        AppUser user = current(session);
        if (user == null) return ResponseEntity.status(401).body(error("Please log in."));
        Booking b = bookings.findById(id).orElse(null);
        if (b == null || (!"ADMIN".equals(user.role) && !Objects.equals(b.userId, user.id)))
            return ResponseEntity.status(404).body(error("Booking not found."));
        if (!"CONFIRMED".equals(b.status)) return ResponseEntity.badRequest().body(error("This booking is already cancelled."));
        b.status = "CANCELLED"; bookings.save(b);
        return ResponseEntity.ok(Map.of("message", "Ticket cancelled. Seat is available again."));
    }

    @GetMapping("/admin/bookings")
    public ResponseEntity<?> allBookings(HttpSession session) {
        AppUser user = current(session);
        if (user == null || !"ADMIN".equals(user.role)) return ResponseEntity.status(403).body(error("Admin access required."));
        return ResponseEntity.ok(bookings.findAllByOrderByCreatedAtDesc());
    }

    @PostMapping("/admin/buses")
    public ResponseEntity<?> addBus(@RequestBody Bus bus, HttpSession session) {
        AppUser user = current(session);
        if (user == null || !"ADMIN".equals(user.role)) return ResponseEntity.status(403).body(error("Admin access required."));
        if (bus.busNumber == null || bus.name == null || bus.source == null || bus.destination == null ||
            bus.departureTime == null || bus.arrivalTime == null || bus.totalSeats < 1 || bus.fare < 0)
            return ResponseEntity.badRequest().body(error("Please complete all bus fields."));
        if (buses.findByBusNumber(bus.busNumber).isPresent())
            return ResponseEntity.status(409).body(error("Bus number already exists."));
        bus.active = true;
        return ResponseEntity.ok(buses.save(bus));
    }

    @PutMapping("/admin/buses/{id}")
    public ResponseEntity<?> updateBus(@PathVariable Long id, @RequestBody Bus input, HttpSession session) {
        AppUser user = current(session);
        if (user == null || !"ADMIN".equals(user.role)) return ResponseEntity.status(403).body(error("Admin access required."));
        Bus b = buses.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        b.name=input.name; b.source=input.source; b.destination=input.destination;
        b.departureTime=input.departureTime; b.arrivalTime=input.arrivalTime;
        b.fare=input.fare; b.totalSeats=input.totalSeats;
        return ResponseEntity.ok(buses.save(b));
    }

    @DeleteMapping("/admin/buses/{id}")
    public ResponseEntity<?> deleteBus(@PathVariable Long id, HttpSession session) {
        AppUser user = current(session);
        if (user == null || !"ADMIN".equals(user.role)) return ResponseEntity.status(403).body(error("Admin access required."));
        Bus b = buses.findById(id).orElse(null);
        if (b == null) return ResponseEntity.notFound().build();
        b.active = false; buses.save(b);
        return ResponseEntity.ok(Map.of("message", "Bus deactivated."));
    }
}
