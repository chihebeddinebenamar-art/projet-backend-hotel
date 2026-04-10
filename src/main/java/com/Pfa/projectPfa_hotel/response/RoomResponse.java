package com.Pfa.projectPfa_hotel.response;

import com.Pfa.projectPfa_hotel.dto.AccessoryDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Data
@NoArgsConstructor
public class RoomResponse {
    private long id;
    private String roomNumber;
    private String roomType;
    private BigDecimal roomPrice;
    private boolean isBooked;
    /** URL HTTPS de la photo principale (Cloudinary). */
    private String photo;
    private List<BookingResponse> bookings;

    /** Capacité max (personnes) depuis le catalogue des types, si trouvé */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer maxOccupancy;

    /** Accessoires liés à la chambre */
    private List<AccessoryDto> accessories;

    /** Galerie (toutes les photos liées à la chambre). */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PhotoResponse> photos;

    public RoomResponse(long id, String roomType, BigDecimal roomPrice) {
        this.id = id;
        this.roomType = roomType;
        this.roomPrice = roomPrice;
    }
}
