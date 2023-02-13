package com.example.tamna.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    private int bookingId;
    private int roomId;
    private String startTime;
    private String endTime;
    private boolean official;

}




