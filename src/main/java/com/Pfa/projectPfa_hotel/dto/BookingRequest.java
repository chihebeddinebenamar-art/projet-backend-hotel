package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String guestFullName;
    private String guestEmail;
    private int numberOfAdults;
    private int numberOfChildren;
    /** Obligatoire si Stripe est configuré côté serveur — paiement réussi vérifié avant la réservation. */
    private String paymentIntentId;
}
