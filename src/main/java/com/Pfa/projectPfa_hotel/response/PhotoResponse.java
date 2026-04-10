package com.Pfa.projectPfa_hotel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {
    private Long id;
    private String url;
    private int sortOrder;
    private boolean primaryPhoto;
}
