package com.Pfa.projectPfa_hotel.controller;

import com.Pfa.projectPfa_hotel.dto.AdminDashboardStats;
import com.Pfa.projectPfa_hotel.dto.DailyOpsStat;
import com.Pfa.projectPfa_hotel.dto.ReceptionDashboardStats;
import com.Pfa.projectPfa_hotel.dto.RoomTypeBookingStat;
import com.Pfa.projectPfa_hotel.model.BookedRoom;
import com.Pfa.projectPfa_hotel.repository.AppUserRepository;
import com.Pfa.projectPfa_hotel.repository.BookingRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class HotelStatsController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping("/admin-dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardStats adminDashboard() {
        LocalDate today = LocalDate.now();
        long totalRooms = roomRepository.count();
        long totalBookings = bookingRepository.count();
        long occupiedToday = bookingRepository.countOccupiedRoomsOnDate(today);
        long checkInsToday = bookingRepository.countByCheckInDate(today);
        long checkOutsToday = bookingRepository.countByCheckOutDate(today);
        long bookingsToday = checkInsToday + checkOutsToday;
        long totalClients = appUserRepository.countByRole("CLIENT");
        long totalReceptionists = appUserRepository.countByRole("RECEPTIONIST");
        BigDecimal avg = roomRepository.averageRoomPrice();
        if (avg == null) {
            avg = BigDecimal.ZERO;
        }
        BigDecimal occupancyRate = percent(occupiedToday, totalRooms);
        long availableRoomsToday = Math.max(0, totalRooms - occupiedToday);
        BigDecimal availableRateToday = percent(availableRoomsToday, totalRooms);

        List<BookedRoom> bookings = bookingRepository.findAllWithRoom();
        BigDecimal revenueThisMonth = estimateRevenueForCurrentMonth(bookings);
        BigDecimal averageLengthOfStayNights = averageLengthOfStayNights(bookings);
        List<RoomTypeBookingStat> topRoomTypes = topRoomTypes(bookings);
        List<DailyOpsStat> last7Days = buildLast7DaysStats(today, totalRooms);

        return new AdminDashboardStats(
                totalRooms,
                totalBookings,
                totalClients,
                totalReceptionists,
                bookingsToday,
                checkInsToday,
                checkOutsToday,
                occupiedToday,
                occupancyRate,
                availableRoomsToday,
                availableRateToday,
                avg,
                revenueThisMonth,
                averageLengthOfStayNights,
                topRoomTypes,
                last7Days
        );
    }

    @GetMapping("/reception-dashboard")
    @PreAuthorize("hasAnyRole('RECEPTIONIST','ADMIN')")
    public ReceptionDashboardStats receptionDashboard() {
        LocalDate today = LocalDate.now();
        long totalRooms = roomRepository.count();
        long occupiedToday = bookingRepository.countOccupiedRoomsOnDate(today);
        long checkInsToday = bookingRepository.countByCheckInDate(today);
        long checkOutsToday = bookingRepository.countByCheckOutDate(today);
        long bookingsToday = checkInsToday + checkOutsToday;
        long availableRooms = Math.max(0, totalRooms - occupiedToday);
        BigDecimal occupancyRate = percent(occupiedToday, totalRooms);
        return new ReceptionDashboardStats(
                bookingsToday,
                checkInsToday,
                checkOutsToday,
                occupiedToday,
                availableRooms,
                occupancyRate
        );
    }

    private BigDecimal estimateRevenueForCurrentMonth(List<BookedRoom> bookings) {
        YearMonth ym = YearMonth.now();
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEndExclusive = ym.plusMonths(1).atDay(1);

        BigDecimal total = BigDecimal.ZERO;
        for (BookedRoom b : bookings) {
            if (b.getCheckInDate() == null || b.getCheckOutDate() == null || b.getRoom() == null) {
                continue;
            }
            LocalDate start = maxDate(b.getCheckInDate(), monthStart);
            LocalDate end = minDate(b.getCheckOutDate(), monthEndExclusive);
            long nights = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            if (nights <= 0) {
                continue;
            }
            BigDecimal roomPrice = b.getRoom().getRoomPrice() == null ? BigDecimal.ZERO : b.getRoom().getRoomPrice();
            total = total.add(roomPrice.multiply(BigDecimal.valueOf(nights)));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageLengthOfStayNights(List<BookedRoom> bookings) {
        long totalNights = 0;
        long count = 0;
        for (BookedRoom b : bookings) {
            if (b.getCheckInDate() == null || b.getCheckOutDate() == null) {
                continue;
            }
            long nights = ChronoUnit.DAYS.between(b.getCheckInDate(), b.getCheckOutDate());
            if (nights <= 0) {
                continue;
            }
            totalNights += nights;
            count++;
        }
        if (count == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(totalNights)
                .divide(BigDecimal.valueOf(count), 1, RoundingMode.HALF_UP);
    }

    private List<DailyOpsStat> buildLast7DaysStats(LocalDate today, long totalRooms) {
        List<DailyOpsStat> out = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long checkIns = bookingRepository.countByCheckInDate(day);
            long checkOuts = bookingRepository.countByCheckOutDate(day);
            long occupied = bookingRepository.countOccupiedRoomsOnDate(day);
            out.add(new DailyOpsStat(
                    day.toString(),
                    checkIns,
                    checkOuts,
                    occupied,
                    percent(occupied, totalRooms)
            ));
        }
        return out;
    }

    private List<RoomTypeBookingStat> topRoomTypes(List<BookedRoom> bookings) {
        Map<String, Long> counts = bookings.stream()
                .filter(b -> b.getRoom() != null && b.getRoom().getRoomType() != null)
                .collect(Collectors.groupingBy(b -> b.getRoom().getRoomType(), Collectors.counting()));
        return counts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(e -> new RoomTypeBookingStat(e.getKey(), e.getValue()))
                .toList();
    }

    private BigDecimal percent(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private LocalDate maxDate(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private LocalDate minDate(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
