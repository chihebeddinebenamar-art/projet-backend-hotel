package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardStats {
    private long totalRooms;
    private long totalBookings;
    private long totalClients;
    private long totalReceptionists;
    private long bookingsToday;
    private long checkInsToday;
    private long checkOutsToday;
    private long inHouseToday;
    private BigDecimal occupancyRateToday;
    private long availableRoomsToday;
    private BigDecimal availableRateToday;
    private BigDecimal averageRoomPrice;
    private BigDecimal estimatedRevenueCurrentMonth;
    private BigDecimal averageLengthOfStayNights;
    private List<RoomTypeBookingStat> topRoomTypes;
    private List<DailyOpsStat> last7Days;
}
