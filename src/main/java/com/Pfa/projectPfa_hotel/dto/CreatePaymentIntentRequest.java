package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreatePaymentIntentRequest {
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
