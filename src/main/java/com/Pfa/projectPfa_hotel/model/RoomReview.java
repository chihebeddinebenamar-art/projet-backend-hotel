package com.Pfa.projectPfa_hotel.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "room_review",
        uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "author_email"})
)
@Getter
@Setter
@NoArgsConstructor
public class RoomReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    /** Email du compte (JWT), pour unicité par chambre */
    @Column(name = "author_email", nullable = false, length = 255)
    private String authorEmail;

    /** Libellé affiché (sans email en clair) */
    @Column(name = "author_display_name", length = 200)
    private String authorDisplayName;

    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
