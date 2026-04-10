package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceptionDashboardStats {
    private long bookingsToday;
    private long checkInsToday;
    private long checkOutsToday;
    private long inHouseToday;
    private long availableRoomsToday;
    private BigDecimal occupancyRateToday;
}
