package com.example.tamna.controller;

import com.example.tamna.mapper.FeedbackMapper;
import com.example.tamna.model.UserDto;
import com.example.tamna.service.AuthService;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackMapper feedbackMapper;
    private final AuthService authService;

    public Date time() {
        long miliseconds = System.currentTimeMillis();
        return new Date(miliseconds);
    }

    @Data
    static class FeedBack{
        private String content;
    }

    @ApiOperation(value = "피드백 내용 보내기")
    @PostMapping(value = "/feedback")
    public ResponseEntity<Map<String, Object>> feedback(@RequestBody FeedBack feedBack, HttpServletResponse response) {
        Date today = time();
        Map<String, Object> map = new HashMap<>();
        UserDto user = authService.checkUser(response);
        String content = feedBack.getContent();

        try {
            if (user != null) {
                int result = feedbackMapper.insertFeedback(user.getUserId(), today, content);
                if (result == 1) {
                    map.put("message", "success");
                } else {
                    map.put("message", "fail");
                }
                return ResponseEntity.status(HttpStatus.OK).body(map);
            }
            map.put("message", "tokenFail");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
        }catch(NullPointerException e){
            map.put("message", "빈 값을 입력해주세요!");
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
    }


    @ApiOperation(value = "피드백 내용 받기", notes = "유저의 Roles가 'DEV'인 경우에만 모든 피드백 보기")
    @GetMapping(value = "/feedback")
    public ResponseEntity<Map<String, Object>> getMyFeedback(HttpServletResponse response){
        UserDto user = authService.checkUser(response);
        Map<String, Object> map = new HashMap<>();

        if(user != null){
            List<String> feedbackData  = user.getRoles().equals("DEV") ? feedbackMapper.findAllFeedback() :  feedbackMapper.findFeedbackById(user.getUserId());
            map.put("feedbackData", feedbackData);
            map.put("userRole", user.getRoles());

            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        map.put("message", "tokenFail");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
    }

    @ApiOperation(value = "피드백 삭제")
    @PostMapping(value = "/feedback/deletion")
    public ResponseEntity<Map<String, Object>> deleteMyFeedback(@RequestBody FeedBack feedBack, HttpServletResponse response){
        UserDto user = authService.checkUser(response);
        Map<String, Object> map = new HashMap<>();
        if(user != null){
            String content = feedBack.getContent();
            int result = feedbackMapper.deleteFeedback(user.getUserId(), content);
            if(result >= 1){
                map.put("message", "삭제가 완료되었습니다.");
            }else {
                map.put("message", "삭제 오류입니다.");
            }
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        map.put("message", "tokenFail");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(map);
    }

}
