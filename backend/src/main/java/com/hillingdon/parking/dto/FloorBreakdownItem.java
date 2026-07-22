package com.hillingdon.parking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FloorBreakdownItem {
    private int floorNumber;
    private String floorName;
    private long occupied;
    private long total;
    private double percentFull;
    private long evTotal;
    private long evOccupied;
    private long vehiclesParked;
}
