package com.Pfa.projectPfa_hotel.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_photo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** URL HTTPS Cloudinary (affichage direct). */
    @Column(nullable = false, length = 2048)
    private String url;

    /** Identifiant Cloudinary (suppression côté hébergeur). */
    @Column(nullable = false, length = 512)
    private String publicId;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primaryPhoto = false;
}
