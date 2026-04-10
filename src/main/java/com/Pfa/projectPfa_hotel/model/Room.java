package com.Pfa.projectPfa_hotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@AllArgsConstructor

public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true, length = 20)
    private String roomNumber;
    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked=false;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @OneToMany(mappedBy = "room"  ,fetch = FetchType.LAZY,  cascade = CascadeType.ALL)
    private List<BookedRoom> bookings;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "room_accessory",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "accessory_id")
    )
    private Set<Accessory> accessories = new HashSet<>();

    public Room() {
        this.bookings = new ArrayList<>();
        this.photos = new ArrayList<>();
    }

    public void addBooking(BookedRoom booking){
        if(bookings==null){
            bookings=new ArrayList<>();
        }
        bookings.add(booking);

        booking.setRoom(this);
        isBooked=true;
        String bookingCode = RandomStringUtils.randomNumeric(10);
        booking.setBookingConfirmationCode(bookingCode);



    }

    public Room get() {
        return this;
    }

}
