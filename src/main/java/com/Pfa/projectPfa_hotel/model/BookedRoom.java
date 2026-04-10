package com.Pfa.projectPfa_hotel.model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BookedRoom {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Column(name ="check_In")
    private LocalDate checkInDate;

    @Column(name ="check_Out")
    private LocalDate checkOutDate;

    @Column(name ="guest_FullName")
    private String guestFullName;

    @Column(name ="guest_Email")
    private String guestEmail;

    @Column(name ="adults")
    private int NumOfAdults;

    @Column(name ="children")
    private int NumOfChildren;

    @Column(name= "total_guest")
    private int totalNumOfGuest;

    @Column(name ="confirmation_Code")
    private String bookingConfirmationCode;

    @Column(name = "checked_in", nullable = false)
    private boolean checkedIn = false;

    @Column(name = "checked_out", nullable = false)
    private boolean checkedOut = false;

    @Column(name = "check_in_registered_at")
    private LocalDateTime checkInRegisteredAt;

    @Column(name = "check_out_registered_at")
    private LocalDateTime checkOutRegisteredAt;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name ="room_id")
    private Room room;


    public void calculateTotalNumberOfGuest() {
        this.totalNumOfGuest = this.NumOfChildren + this.NumOfAdults;
    }

    public void setNumOfAdults(int numOfAdults) {
        NumOfAdults = numOfAdults;
        calculateTotalNumberOfGuest();
    }

    public void setNumOfChildren(int numOfChildren) {
        NumOfChildren = numOfChildren;
        calculateTotalNumberOfGuest();
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

    public BookedRoom(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }
}
