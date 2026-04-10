package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.CreatePaymentIntentRequest;
import com.Pfa.projectPfa_hotel.dto.CreatePaymentIntentResponse;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.service.StripePaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody CreatePaymentIntentRequest req) {
        try {
            if (req.getRoomId() == null || req.getCheckInDate() == null || req.getCheckOutDate() == null) {
                return ResponseEntity.badRequest().body("roomId, checkInDate et checkOutDate sont requis.");
            }
            CreatePaymentIntentResponse body = stripePaymentService.createPaymentIntent(
                    req.getRoomId(),
                    req.getCheckInDate(),
                    req.getCheckOutDate());
            return ResponseEntity.ok(body);
        } catch (BadRequestException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", "Stripe : " + e.getMessage()));
        }
    }
}
