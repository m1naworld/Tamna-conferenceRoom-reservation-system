package com.example.tamna.controller;

import com.example.tamna.RoomType;
import com.example.tamna.dto.*;

import com.example.tamna.model.Room;
import com.example.tamna.model.UserDto;
import com.example.tamna.service.*;

import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final Logger LOGGER = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;
    private final RoomService roomService;
    private final UserService userService;
    private final ParticipantsService participantsService;
    private final AuthService authService;


    @ApiOperation(value = "예약페이지 데이터", notes = "회의실 전체 데이터, 유저데이터, 현재 회의실데이터, 예약데이터, 유저들 이름 목록 데이터")
    @GetMapping(value = "")
    public ResponseEntity<Map<String, Object>> getRoomBookingState(@RequestParam("roomId") int roomId, HttpServletResponse response) {
            UserDto user = authService.checkUser(response);
            Map<String, Object> map = new HashMap<>();
        if (user != null) {
            if(user.getFloor() == 2 || user.getFloor() == 3 || user.getFloor() == 4){
                map.put("roomData", roomService.getFloorRoom(user.getFloor()));
            }else{
                map.put("roomData", roomService.roomList());
            }
            map.put("userData", userService.getUserData(user.getUserId()));
            map.put("currentRoomData", roomService.getRoomId(roomId));
            map.put("bookingData", bookingService.roomBookingState(roomId));
            map.put("namesData", userService.getUserNames(user.getClasses()));
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }else{
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        }
    };

    @ApiOperation(value = "예약 현황 페이지 데이터", notes = "회의실데이터, 예약데이터")
    @GetMapping(value = "/details-booking")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBookingState(HttpServletResponse response) {
        UserDto user = authService.checkUser(response);
        Map<String, Object> map = new HashMap<>();
        if(user != null){
            if(user.getFloor() == 2 || user.getFloor() == 3 ) {
                map.put("RoomData", roomService.getFloorRoom(user.getFloor()));
                map.put("BookingData", bookingService.floorDetailBookingData(user.getFloor()));
            }else{
                List<Room> roomData = new ArrayList<>(roomService.getFloorRoom(2));
                roomData.addAll(roomService.getFloorRoom(3));
                roomData.addAll(roomService.getFloorRoom(4));

                List<DetailBookingDataDto> bookingData = new ArrayList<>(bookingService.floorDetailBookingData(2));
                bookingData.addAll(bookingService.floorDetailBookingData(3));
                bookingData.addAll(bookingService.floorDetailBookingData(4));

                map.put("RoomData", roomData);
                map.put("BookingData", bookingData);
            }
            map.put("floor", user.getFloor());
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }else{
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        }

    }


    @ApiOperation(value = "메인페이지 데이터", notes = "층수데이터, 회의실데이터, 예약데이터")
    @GetMapping(value = "/main")
    public ResponseEntity<Map<String, Object>> getMainData(HttpServletResponse response){
        Map<String, Object> map = new HashMap<>();

        UserDto user = authService.checkUser(response);
        if(user != null){
            if(user.getFloor() == 2 || user.getFloor() == 3 || user.getFloor() == 4) {
                map.put("RoomData", roomService.getFloorRoom(user.getFloor()));
                map.put("BookingData", bookingService.floorBookingData(user.getFloor()));
            }else{
                map.put("RoomData", roomService.roomList());
                map.put("BookingData", bookingService.allRoomBookingState());
            }
            map.put("floor", user.getFloor());
            return ResponseEntity.status(HttpStatus.OK).body(map);

        }else{
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        }
    }


    @ApiOperation(value = "예약하기", notes = "보내는 데이터: 회의실아이디, 회의실타입, 시작시간, 종료시간, 팀원데이터")
    @PostMapping(value = "/conference")
    public ResponseEntity<Map<String, Object>> conferenceRoomBooking(@RequestBody PostBookingDataDto postBookingDataDto, HttpServletResponse response) {

        UserDto user = authService.checkUser(response);
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> arr = new HashMap<>();

        if (user == null) {
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        } // 토큰이 유효한 유저인 경우

        // 인재인 경우
        if(user.getClasses() != 0) {
            String roomTypeUpper = postBookingDataDto.getRoomType().toUpperCase();
            RoomType roomType = RoomType.valueOf(roomTypeUpper);
            System.out.println(roomType);


            // 회의실, 스튜디오 예약 횟수 초과했는지 나박스 예약 최대시간 초과했는지 확인
            Map<Boolean, String> checkBooking = participantsService.checkBookingUser(roomType.lowerCase, user.getUserId());
            // 예약 횟수 및 예약 최대시간 꽉 찬 경우
            if (checkBooking.containsKey(false)) {
                if (roomType.lowerCase.equals(RoomType.NABOX.lowerCase)) {
                    arr.put("fail", roomType.name + "예약은 하루 최대 두시간만 가능합니다.");
                } else {
                    arr.put("fail", roomType.name + "예약은 하루에 한번만 가능합니다.");
                }
                map.put("message", arr);
                return ResponseEntity.status(HttpStatus.OK).body(map);

            }

            // 동시간대 예약자 있는지 확인
            Set<String> usingUsers = participantsService.checkUsingBooking(user, postBookingDataDto);
            // 동시간대 예약자가 있는 경우
            if (!usingUsers.isEmpty()) {
                if (roomType.lowerCase.equals(RoomType.MEETING.lowerCase)) {
                    arr.put("fail", "현재 동시간대 예약중인" + usingUsers + "님이 포함되어 있습니다.");
                } else {
                    arr.put("fail", "동시간대 다른 예약은 불가합니다.");
                }
                map.put("message", arr);
                return ResponseEntity.status(HttpStatus.OK).body(map);
            }

            // 예약된 회의실인지 확인
            boolean usingRoom = bookingService.findSameBooking(postBookingDataDto.getRoomId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime());
            // 이미 예약된 회의실일 경우
            if (usingRoom) {
                arr.put("fail", "❌ 이미 예약이 완료되어 예약이 불가합니다.");
                map.put("message", arr);
                return ResponseEntity.status(HttpStatus.OK).body(map);
            }


            // 나박스가 아닌 회의실, 스튜디오 예약 로직
            if (!roomType.lowerCase.equals(RoomType.NABOX.lowerCase)) {
                int bookingId = bookingService.insertBooking(postBookingDataDto.getRoomId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime(), false);
                // 회의실인 경우
                if (roomType.lowerCase.equals(RoomType.MEETING.lowerCase)) {
                    List<UserDto> users = userService.getUsersData(user.getClasses(), user.getUserName(), postBookingDataDto.getTeamMate());
                    participantsService.insertParticipants(bookingId, users, postBookingDataDto.getTeamMate());
                    arr.put("success", "회의실 예약 성공! ♥ ");
                } else {
                    // 스튜디오인 경우
                    participantsService.insertApplicant(bookingId, user.getUserId());
                    arr.put("success", "스튜디오 예약 성공! 비밀번호는 매니저님께 문의해주세요! ♥");
                }
                map.put("message", arr);
                return ResponseEntity.status(HttpStatus.OK).body(map);
            }

            // 나박스 예약 로직
            boolean timeResult = participantsService.checkUsingTime(postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime());
            if (checkBooking.containsValue("add") && !timeResult) {
                arr.put("fail", "나박스 하루 최대 이용시간은  입니다.");
            } else {
                int bookingId = bookingService.insertBooking(postBookingDataDto.getRoomId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime(), false);
                participantsService.insertApplicant(bookingId, user.getUserId());
                arr.put("success", roomType + " 예약 성공! ♥ ");
            }
            map.put("message", arr);
            return ResponseEntity.status(HttpStatus.OK).body(map);

        } // 매니저인 경우

        // 4층이 아닐경우 공식일정
        if (postBookingDataDto.getRoomId() / 100 != 4) {
            // 공식일정 예약 확인
            List<Boolean> checkOfficial = bookingService.checkOfficial(postBookingDataDto.getRoomId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime());

            // 이미 공식 예약인 경우
            if (!checkOfficial.isEmpty()) {
                arr.put("fail", "이미 공식일정이 등록되어있습니다.");
            } else { // 없을 경우
                // 기존 인재들 예약 cancel상태로 변경 및 공식 일정 예약
                int resultBookingId = bookingService.updateBooking(postBookingDataDto.getRoomId(), user.getUserId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime(), true);
                // 예약자 등록
                List<UserDto> users = userService.getUsersData(user.getClasses(), user.getUserName(), postBookingDataDto.getTeamMate());
                participantsService.insertParticipants(resultBookingId, users, postBookingDataDto.getTeamMate());
                arr.put("success", "공식 일정 등록 완료 ✅");
            }
            map.put("message", arr);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        } // 4층일 경우

        // 회의실이 예약되었는지 확인
        boolean usingRoom = bookingService.findSameBooking(postBookingDataDto.getRoomId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime());
        if (usingRoom) {
            arr.put("fail", "이미 예약이 완료된 회의실 입니다.");
        } else {
            int bookingId = bookingService.insertBooking(postBookingDataDto.getRoomId(), postBookingDataDto.getStartTime(), postBookingDataDto.getEndTime(), false);
            // 유저들 이름 종합
            List<UserDto> users = userService.getUsersData(user.getClasses(), user.getUserName(), postBookingDataDto.getTeamMate());
            participantsService.insertParticipants(bookingId, users, postBookingDataDto.getTeamMate());
            arr.put("success", " 회의실 예약 성공! ♥ ");
        }
        map.put("message", arr);
        return ResponseEntity.status(HttpStatus.OK).body(map);

    }


    @Data
    static class BookingId{
        private int bookingId;
    }

    @ApiOperation(value = "마이페이지 예약 취소", notes = "보내는 데이터: bookingId")
    @PostMapping(value ="/cancellation")
    public ResponseEntity<Map<String, Object>> cancelBooking(@RequestBody BookingId bookingId, HttpServletResponse response){
        Map<String, Object> map = new HashMap<>();

        UserDto user = authService.checkUser(response);

        if(user!= null) {
            int intBookingId = bookingId.bookingId;
            CancelDto booking = bookingService.selectBookingId(intBookingId, user.getUserId());
            String checkCancel;

            if(booking != null) {
                if (!booking.isOfficial()) {
                    checkCancel = bookingService.deleteBooking(intBookingId);
                } else {
                    checkCancel = bookingService.deleteOfficialBooking(booking);
                }

                // 결과 반환
                if (checkCancel.equals("success")) {
                    map.put(checkCancel, "예약 취소가 완료되었습니다");
                } else {
                    map.put(checkCancel, "예약 취소에 실패하였습니다.");
                }

                return ResponseEntity.status(HttpStatus.OK).body(map);
            }else{
                map.put("message", "예약 취소 오류");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
            }
        }else{
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        }
    }


}
