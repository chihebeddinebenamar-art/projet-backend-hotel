package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDashboardStats {
    private long totalRooms;
    private long totalBookings;
    private long bookedRoomCount;
    private BigDecimal averageRoomPrice;
}
