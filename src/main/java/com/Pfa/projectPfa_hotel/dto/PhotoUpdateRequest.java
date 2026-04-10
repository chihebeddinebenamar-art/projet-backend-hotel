package com.Pfa.projectPfa_hotel.dto;

import lombok.Data;

@Data
public class PhotoUpdateRequest {
    private Integer sortOrder;
    /** Si true, cette photo devient la principale (les autres sont mises à jour). */
    private Boolean primaryPhoto;
}
