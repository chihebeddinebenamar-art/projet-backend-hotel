package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedResponse {
    private String confirmationCode;
    private String message;
}
