package com.Pfa.projectPfa_hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyOpsStat {
    private String date;
    private long checkIns;
    private long checkOuts;
    private long occupiedRooms;
    private BigDecimal occupancyRate;
}
