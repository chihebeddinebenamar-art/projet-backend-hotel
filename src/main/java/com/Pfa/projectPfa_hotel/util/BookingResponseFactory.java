package com.Pfa.projectPfa_hotel.util;

import com.Pfa.projectPfa_hotel.model.BookedRoom;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.response.BookingResponse;
import com.Pfa.projectPfa_hotel.service.IRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingResponseFactory {

    private final IRoomService roomService;

    public BookingResponse toResponse(BookedRoom booking) {
        Room theRoom = roomService.getRoomById(booking.getRoom().getId());
        return new BookingResponse(
                booking.getBookingId(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getGuestFullName(),
                booking.getGuestEmail(),
                booking.getNumOfAdults(),
                booking.getNumOfChildren(),
                booking.getTotalNumOfGuest(),
                booking.getBookingConfirmationCode(),
                booking.isCheckedIn(),
                booking.isCheckedOut(),
                booking.getCheckInRegisteredAt(),
                booking.getCheckOutRegisteredAt(),
                RoomMapper.toResponse(theRoom));
    }
}
