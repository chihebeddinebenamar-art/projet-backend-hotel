package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentIntentResponse {
    private String clientSecret;
    private String paymentIntentId;
    private long amountCents;
    private String currency;
}
