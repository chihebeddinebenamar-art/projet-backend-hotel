package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.BookingCreatedResponse;
import com.Pfa.projectPfa_hotel.dto.BookingRequest;
import com.Pfa.projectPfa_hotel.dto.OccupiedRangeResponse;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.InvalidBookingRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.BookedRoom;
import com.Pfa.projectPfa_hotel.response.BookingResponse;
import com.Pfa.projectPfa_hotel.service.IBookingService;
import com.Pfa.projectPfa_hotel.service.StripePaymentService;
import com.Pfa.projectPfa_hotel.util.BookingResponseFactory;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final IBookingService bookingService;
    private final StripePaymentService stripePaymentService;
    private final BookingResponseFactory bookingResponseFactory;

    @GetMapping("/all-bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream().map(bookingResponseFactory::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Réception + admin : planning global des réservations. */
    @GetMapping("/staff/all")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookingsForStaff() {
        List<BookedRoom> bookings = bookingService.getAllBookings();
        List<BookingResponse> responses = bookings.stream().map(bookingResponseFactory::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Client connecté (JWT) : réservations dont l’email invité = email du token. */
    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(Principal principal) {
        List<BookedRoom> list = bookingService.getBookingsForGuestEmail(principal.getName());
        List<BookingResponse> responses = list.stream().map(bookingResponseFactory::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Réception / admin : séjours du jour (arrivées, départs, présents). */
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<List<BookingResponse>> todayBookings() {
        List<BookedRoom> list = bookingService.getBookingsForReceptionDate(LocalDate.now());
        List<BookingResponse> responses = list.stream().map(bookingResponseFactory::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Réception + admin : détail client + historique selon email. */
    @GetMapping("/staff/client-history")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<List<BookingResponse>> clientHistory(@RequestParam String email) {
        List<BookedRoom> list = bookingService.getBookingsForGuestEmail(email);
        List<BookingResponse> responses = list.stream().map(bookingResponseFactory::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/room/{roomId}/bookings")
    public ResponseEntity<List<BookingResponse>> getBookingsForRoom(@PathVariable Long roomId) {
        List<BookedRoom> bookings = bookingService.getAllBookingsByRoomId(roomId);
        List<BookingResponse> responses = bookings.stream().map(bookingResponseFactory::toResponse).toList();
        return ResponseEntity.ok(responses);
    }

    /** Plages déjà réservées (calendrier) — sans données personnelles. */
    @GetMapping("/room/{roomId}/occupied-ranges")
    public ResponseEntity<List<OccupiedRangeResponse>> getOccupiedRanges(@PathVariable Long roomId) {
        List<BookedRoom> bookings = bookingService.getAllBookingsByRoomId(roomId);
        List<OccupiedRangeResponse> list = bookings.stream()
                .map(b -> new OccupiedRangeResponse(b.getCheckInDate(), b.getCheckOutDate()))
                .toList();
        return ResponseEntity.ok(list);
    }

    /** Vérifie si la chambre est libre sur [checkIn, checkOut). */
    @GetMapping("/room/{roomId}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long roomId,
            @RequestParam LocalDate checkIn,
            @RequestParam LocalDate checkOut) {
        boolean available = bookingService.isRoomAvailableForDates(roomId, checkIn, checkOut);
        Map<String, Object> body = new HashMap<>();
        body.put("available", available);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/confirmation/{confirmationCode}")
    public ResponseEntity<?> getBookingByConfirmationCode(@PathVariable String confirmationCode) {
        try {
            BookedRoom booking = bookingService.findByBookingConfirmationCode(confirmationCode);
            BookingResponse bookingResponse = bookingResponseFactory.toResponse(booking);
            return ResponseEntity.ok(bookingResponse);
        } catch (RessourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping(value = "/room/{roomId}/booking", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveBooking(@PathVariable Long roomId, @RequestBody BookingRequest bookingRequest) {
        try {
            if (stripePaymentService.isConfigured()) {
                stripePaymentService.verifyPaymentForBooking(
                        roomId,
                        bookingRequest.getPaymentIntentId(),
                        bookingRequest.getCheckInDate(),
                        bookingRequest.getCheckOutDate());
            }
            String confirmationCode = bookingService.saveBooking(roomId, bookingRequest);
            return ResponseEntity.ok(new BookingCreatedResponse(
                    confirmationCode,
                    "Réservation enregistrée avec succès."
            ));
        } catch (InvalidBookingRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erreur Stripe : " + e.getMessage());
        }
    }

    @DeleteMapping("/booking/{bookingId}/delete")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId) {
        try {
            bookingService.cancelBooking(bookingId);
            return ResponseEntity.noContent().build();
        } catch (RessourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }
    }

    @PostMapping("/booking/{bookingId}/check-in")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<?> registerCheckIn(@PathVariable Long bookingId) {
        try {
            BookedRoom booking = bookingService.registerCheckIn(bookingId);
            return ResponseEntity.ok(bookingResponseFactory.toResponse(booking));
        } catch (RessourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (BadRequestException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/booking/{bookingId}/check-out")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ResponseEntity<?> registerCheckOut(@PathVariable Long bookingId) {
        try {
            BookedRoom booking = bookingService.registerCheckOut(bookingId);
            return ResponseEntity.ok(bookingResponseFactory.toResponse(booking));
        } catch (RessourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (BadRequestException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

}
