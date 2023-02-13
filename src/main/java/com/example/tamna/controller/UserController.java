package com.example.tamna.controller;

import com.example.tamna.dto.DetailBookingDataDto;
import com.example.tamna.model.UserDto;
import com.example.tamna.service.AuthService;
import com.example.tamna.service.BookingService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final BookingService bookingService;
    private final AuthService authService;


    @ApiOperation(value="유저 데이터")
    @GetMapping("/data")
    public ResponseEntity<Map<String, Object>> resUserData(HttpServletResponse response){
        UserDto user = authService.checkUser(response);
        Map<String, Object> map = new HashMap<>();
        if(user != null) {
            map.put("userData", user);
            return ResponseEntity.status(HttpStatus.OK).body(map);
        } map.put("message", "tokenFail");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
    }


    @ApiOperation(value = "마이페이지 데이터")
    @GetMapping(value = "/mypage")
    public ResponseEntity<Map<String, Object>> myBookingState(HttpServletResponse response){
        UserDto user = authService.checkUser(response);
        Map<String, Object> map = new HashMap<>();

        if(user != null) {
            map.put("userData", user);
            List<DetailBookingDataDto> myBookingDetailDataList = bookingService.userIncludedBooking(user.getUserId());
            map.put("myBookingDetailDataList", myBookingDetailDataList);

            return ResponseEntity.status(HttpStatus.OK).body(map);
        }else{
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        }
    }



}
