package com.Pfa.projectPfa_hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hotel_room_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    /** Capacité maximale (nombre de personnes) pour ce type de chambre */
    @Column(nullable = false)
    private int maxOccupancy;
}
