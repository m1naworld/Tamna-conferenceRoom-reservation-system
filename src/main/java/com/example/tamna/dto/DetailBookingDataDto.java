package com.example.tamna.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

import com.example.tamna.model.JoinBooking;

@Data
@NoArgsConstructor
public class DetailBookingDataDto {
    private int bookingId;
    private int roomId;
    private String roomName;
    private String roomType;
    private String startTime;
    private String endTime;
    private Map<String, String> applicant;
    private List<String> participants;
    private String mode;
    private boolean official;


    public void updateDetailBookingDataDto(JoinBooking joinBooking) {
        this.bookingId = joinBooking.getBookingId();
        this.roomId = joinBooking.getRoomId();
        this.roomName = joinBooking.getRoomName();
        this.roomType = joinBooking.getRoomType();
        this.startTime = joinBooking.getStartTime();
        this.endTime = joinBooking.getEndTime();
        this.mode = joinBooking.getMode();
        this.official = joinBooking.isOfficial();
    }

    public void updateApplication(Map<String, String> applicant) {
        this.applicant = applicant;
    }

    public void updateParticipants(List<String> teamMate) {
        this.participants = teamMate;
    }

}
