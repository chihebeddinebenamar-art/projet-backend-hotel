package com.Pfa.projectPfa_hotel.service;

import com.Pfa.projectPfa_hotel.dto.BookingRequest;
import com.Pfa.projectPfa_hotel.exception.BadRequestException;
import com.Pfa.projectPfa_hotel.exception.InvalidBookingRequestException;
import com.Pfa.projectPfa_hotel.exception.RessourceNotFoundException;
import com.Pfa.projectPfa_hotel.model.BookedRoom;
import com.Pfa.projectPfa_hotel.model.Room;
import com.Pfa.projectPfa_hotel.repository.BookingRepository;
import com.Pfa.projectPfa_hotel.repository.HotelRoomTypeRepository;
import com.Pfa.projectPfa_hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService implements IBookingService {
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final IRoomService roomService;
    private final HotelRoomTypeRepository hotelRoomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookedRoom> getAllBookings() {
        return bookingRepository.findAllWithRoom();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookedRoom> getBookingsForGuestEmail(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return bookingRepository.findByGuestEmailWithRoom(email.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookedRoom> getBookingsForReceptionDate(LocalDate date) {
        if (date == null) {
            return List.of();
        }
        return bookingRepository.findActiveOnDateWithRoom(date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookedRoom> getAllBookingsByRoomId(Long roomId) {
        return bookingRepository.findByRoomIdWithRoom(roomId);
    }

    @Override
    @Transactional
    public String saveBooking(Long roomId, BookingRequest request) {
        if (request.getNumberOfChildren() < 0) {
            throw new InvalidBookingRequestException("Le nombre d’enfants ne peut pas être négatif.");
        }
        if (request.getNumberOfAdults() < 1) {
            throw new InvalidBookingRequestException("Au moins un adulte est requis.");
        }
        if (request.getCheckOutDate() == null || request.getCheckInDate() == null) {
            throw new InvalidBookingRequestException("Les dates de séjour sont obligatoires.");
        }
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())
                || request.getCheckOutDate().isEqual(request.getCheckInDate())) {
            throw new InvalidBookingRequestException("La date d'arrivée doit être strictement avant la date de départ.");
        }
        Room room = roomService.getRoomById(roomId);
        List<BookedRoom> existing = room.getBookings() != null ? room.getBookings() : List.of();

        BookedRoom booking = new BookedRoom();
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuestFullName(request.getGuestFullName());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setNumOfAdults(request.getNumberOfAdults());
        booking.setNumOfChildren(request.getNumberOfChildren());
        booking.calculateTotalNumberOfGuest();

        hotelRoomTypeRepository.findByNameIgnoreCase(room.getRoomType()).ifPresent(t -> {
            if (booking.getTotalNumOfGuest() > t.getMaxOccupancy()) {
                throw new InvalidBookingRequestException(
                        "Le nombre de personnes (" + booking.getTotalNumOfGuest()
                                + ") dépasse la capacité maximale (" + t.getMaxOccupancy()
                                + ") pour ce type de chambre.");
            }
        });

        if (!roomIsAvailable(booking, existing)) {
            throw new InvalidBookingRequestException("Cette chambre n'est pas disponible sur la période choisie.");
        }

        room.addBooking(booking);
        bookingRepository.save(booking);
        roomRepository.save(room);
        return booking.getBookingConfirmationCode();
    }

    @Override
    public void cancelBooking(Long bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            throw new RessourceNotFoundException("Réservation introuvable.");
        }
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookedRoom findByBookingConfirmationCode(String confirmationCode) {
        BookedRoom booking = bookingRepository.findByBookingConfirmationCode(confirmationCode);
        if (booking == null) {
            throw new RessourceNotFoundException("Aucune réservation pour ce code.");
        }
        return booking;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailableForDates(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return false;
        }
        Room room = roomService.getRoomById(roomId);
        List<BookedRoom> existing = room.getBookings() != null ? room.getBookings() : List.of();
        BookedRoom probe = new BookedRoom();
        probe.setCheckInDate(checkIn);
        probe.setCheckOutDate(checkOut);
        return roomIsAvailable(probe, existing);
    }

    @Override
    @Transactional
    public BookedRoom registerCheckIn(Long bookingId) {
        BookedRoom booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RessourceNotFoundException("Réservation introuvable."));
        LocalDate today = LocalDate.now();
        if (!today.equals(booking.getCheckInDate())) {
            throw new BadRequestException("L'arrivée peut être enregistrée uniquement le jour d'arrivée.");
        }
        if (booking.isCheckedIn()) {
            throw new BadRequestException("Arrivée déjà enregistrée pour cette réservation.");
        }
        booking.setCheckedIn(true);
        booking.setCheckInRegisteredAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public BookedRoom registerCheckOut(Long bookingId) {
        BookedRoom booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RessourceNotFoundException("Réservation introuvable."));
        LocalDate today = LocalDate.now();
        if (!today.equals(booking.getCheckOutDate())) {
            throw new BadRequestException("Le départ peut être enregistré uniquement le jour du départ.");
        }
        if (booking.isCheckedOut()) {
            throw new BadRequestException("Départ déjà enregistré pour cette réservation.");
        }
        if (!booking.isCheckedIn()) {
            throw new BadRequestException("Enregistrez d'abord l'arrivée avant de valider le départ.");
        }
        booking.setCheckedOut(true);
        booking.setCheckOutRegisteredAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    /**
     * Chevauchement d'intervalles de dates [checkIn, checkOut) — même logique que les calendriers hôteliers.
     */
    private boolean roomIsAvailable(BookedRoom candidate, List<BookedRoom> existingBookings) {
        LocalDate aIn = candidate.getCheckInDate();
        LocalDate aOut = candidate.getCheckOutDate();
        for (BookedRoom existing : existingBookings) {
            if (datesOverlap(aIn, aOut, existing.getCheckInDate(), existing.getCheckOutDate())) {
                return false;
            }
        }
        return true;
    }

    private boolean datesOverlap(LocalDate aIn, LocalDate aOut, LocalDate bIn, LocalDate bOut) {
        return aIn.isBefore(bOut) && bIn.isBefore(aOut);
    }
}
