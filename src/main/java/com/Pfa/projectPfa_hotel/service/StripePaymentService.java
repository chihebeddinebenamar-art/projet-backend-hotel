package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.CreatePaymentIntentResponse;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.model.Room;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final IRoomService roomService;

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    public boolean isConfigured() {
        return stripeSecretKey != null && !stripeSecretKey.isBlank();
    }

    public long computeAmountCents(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        Room room = roomService.getRoomById(roomId);
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights < 1) {
            throw new BadRequestException("Le séjour doit comporter au moins une nuit.");
        }
        BigDecimal total = room.getRoomPrice().multiply(BigDecimal.valueOf(nights));
        return total.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    public CreatePaymentIntentResponse createPaymentIntent(Long roomId, LocalDate checkIn, LocalDate checkOut)
            throws StripeException {
        if (!isConfigured()) {
            throw new BadRequestException("Paiement Stripe non configuré (variable STRIPE_SECRET_KEY / stripe.secret-key).");
        }
        long amountCents = computeAmountCents(roomId, checkIn, checkOut);
        if (amountCents < 50) {
            throw new BadRequestException("Montant insuffisant (minimum Stripe : 0,50 €).");
        }
        Stripe.apiKey = stripeSecretKey;
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("eur")
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .putMetadata("roomId", String.valueOf(roomId))
                .putMetadata("checkInDate", checkIn.toString())
                .putMetadata("checkOutDate", checkOut.toString())
                .build();
        PaymentIntent intent = PaymentIntent.create(params);
        return new CreatePaymentIntentResponse(
                intent.getClientSecret(),
                intent.getId(),
                amountCents,
                "eur");
    }

    /**
     * Vérifie que l’intent est payé et correspond à la chambre / dates / montant attendu.
     */
    public void verifyPaymentForBooking(Long roomId, String paymentIntentId, LocalDate checkIn, LocalDate checkOut)
            throws StripeException {
        if (!isConfigured()) {
            return;
        }
        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new BadRequestException("Paiement requis : finalisez le paiement par carte avant la réservation.");
        }
        Stripe.apiKey = stripeSecretKey;
        PaymentIntent pi = PaymentIntent.retrieve(paymentIntentId);
        if (!"succeeded".equals(pi.getStatus())) {
            throw new BadRequestException("Le paiement n’est pas confirmé (statut : " + pi.getStatus() + ").");
        }
        if (pi.getMetadata() == null) {
            throw new BadRequestException("Métadonnées de paiement invalides.");
        }
        if (!String.valueOf(roomId).equals(pi.getMetadata().get("roomId"))) {
            throw new BadRequestException("Le paiement ne correspond pas à cette chambre.");
        }
        if (!checkIn.toString().equals(pi.getMetadata().get("checkInDate"))
                || !checkOut.toString().equals(pi.getMetadata().get("checkOutDate"))) {
            throw new BadRequestException("Le paiement ne correspond pas aux dates de séjour.");
        }
        long expected = computeAmountCents(roomId, checkIn, checkOut);
        if (pi.getAmount() != null && pi.getAmount() != expected) {
            throw new BadRequestException("Le montant payé ne correspond pas au total du séjour.");
        }
    }
}
