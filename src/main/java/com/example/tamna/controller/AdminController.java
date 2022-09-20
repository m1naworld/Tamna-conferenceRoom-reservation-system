package com.example.tamna.controller;

import com.example.tamna.dto.ClassFloorDto;
import com.example.tamna.dto.RoomTimeDto;
import com.example.tamna.model.UserDto;
import com.example.tamna.service.AdminService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @ApiOperation(value="최신기수 업데이트")
    @PostMapping("/update/user")
    public ResponseEntity<Map<String, Object>> insertUserData(@RequestPart(required = false) MultipartFile file, HttpServletRequest request) throws IOException {
        String resourceSrc = request.getServletContext().getRealPath("/data/");
        File dest = new File(resourceSrc + file.getOriginalFilename());
        file.transferTo(dest);
        String result = adminService.updateUser(dest);

        Map<String, Object> map = new HashMap<>();
        if(result.equals("success")){
            map.put("message", "최신기수 업로드가 완료되었습니다.");
        }else{
            map.put("message", "파일 오류<빈 파일인지, 파일 양식이 옳은지 확인 혹은 파일명을 바꿔주세요!>");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "기수별 층수 보기")
    @GetMapping("/view/class&floor")
    public ResponseEntity<Map<String, Object>> getClassOfFloor(){
        Map<String, Object> map = new HashMap<>();
        List<ClassFloorDto> result = adminService.getClassOfFloorData();
        if(!result.isEmpty()) {
            map.put("ClassOfFloorData", result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "기수별 층수 데이터 바꾸기", notes = "기수가 0으로 들어올 경우 fail 처리")
    @PostMapping("/change/floor")
    public ResponseEntity<Map<String, Object>> changeFloor(@RequestBody ClassFloorDto changeFloorDto){
        Map<String, Object> map = new HashMap<>();
        String result = adminService.updateClassOfFloorData(changeFloorDto);
        if(result.equals("success")){
            System.out.println(changeFloorDto.getClasses() + "기 가 " + changeFloorDto.getFloor() + "로 변경되었음.");
            map.put("message", "층수 변경이 완료되었습니다!");
        }else{
            map.put("message", "fail");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "회의실별 최대시간", notes = "floor가 2,3이 아닌경우 fail 처리")
    @GetMapping("/view/room")
    public ResponseEntity<Map<String, Object>> getRoomData(@RequestParam("floor")int floor){
        Map<String, Object> map = new HashMap<>();
        if(floor == 2 || floor == 3){
            adminService.getRoomData(floor);
            map.put("RoomData", adminService.getRoomData(floor));
        }else{
            map.put("RoomData", "fail");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "회의실별 최대 시간 수정", notes = "floor가 2,3이 아닌경우 변경불가 메시지")
    @PostMapping("/change/maxtime")
    public ResponseEntity<Map<String, Object>> updateRoomTIme(@RequestBody RoomTimeDto roomTimeDto){
        Map<String, Object> map = new HashMap<>();
        if(roomTimeDto.getFloor() == 2 || roomTimeDto.getFloor() == 3) {
            String result = adminService.updateRoomTime(roomTimeDto);
            if (result.equals("success")) {
                map.put("message", "최대 시간 변경이 완료되었습니다.");
            } else {
                map.put("message", result);
            }
            return ResponseEntity.status(HttpStatus.OK).body(map);
        }
        map.put("message", "2,3층만 변경 가능합니다.");
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


    @ApiOperation(value = "전체 기수리스트 보내기")
    @GetMapping("/view/class-list")
    public ResponseEntity<Map<String, Object>> getAllClassList(){
        Map<String, Object> map = new HashMap<>();
        List<Integer> classListResult = adminService.allUserClass();
        map.put("ClassList", classListResult);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value="기수별 유저데이터 보내기")
    @GetMapping("/view/user")
    public ResponseEntity<Map<String, Object>> getAllUserData(@RequestParam("classes") int classes) {
        Map<String, Object> map = new HashMap<>();
        List<UserDto> result = adminService.allUserData(classes);
        map.put("AllUserData", result);
       return ResponseEntity.status(HttpStatus.OK).body(map);
    }



}
