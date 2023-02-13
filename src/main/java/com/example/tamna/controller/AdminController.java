package com.example.tamna.controller;

import com.example.tamna.dto.ClassFloorDto;
import com.example.tamna.dto.RoomDto;
import com.example.tamna.mapper.AdminMapper;
import com.example.tamna.model.Room;
import com.example.tamna.model.UserDto;
import com.example.tamna.service.AdminService;
import com.example.tamna.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final AdminMapper adminMapper;
    private final UserService userService;

    @Value("${ADMIN_HEADER}")
    private String ADMINAUTHORIZATION_HEADER;

    @Value("${jwt.token-prefix}")
    private String tokenPrefix;


    private List<Integer> floor = Arrays.asList(0, 2, 3);

    @Data
    static class UserId{
        private String userId;
    }

    @ApiOperation(value= "어드민 로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginAdmin(@RequestBody UserId userId, HttpServletResponse response){

        Map<String, Object> map = new HashMap<>();
        String getUserId = userId.userId;
        String result = adminService.getAdminToken(getUserId);

        if(result.equals("fail")){
            map.put("message", result);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
        }
        response.addHeader(ADMINAUTHORIZATION_HEADER, tokenPrefix + result);
        map.put("message", "success");

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "최신기수 페이지")
    @GetMapping("/insert/user")
    public ResponseEntity<Map<String, Object>> insertUserData(){
        Map<String, Object> map = new HashMap<>();
        map.put("message", "success");
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }



    @ApiOperation(value="최신기수 업데이트")
    @PostMapping("/insert/user")
    public ResponseEntity<Map<String, Object>> insertUserData(@RequestPart(required = false) MultipartFile file, HttpServletRequest request) throws IOException {
//        String resourceSrc = request.getServletContext().getRealPath("/data/"); test용
        String resourceSrc = "/home/opc/server/resources/"; // 서버 파일 절대경로

        Map<String, Object> map = new HashMap<>();

        try {
            File dest = new File(resourceSrc + file.getOriginalFilename());
            file.transferTo(dest);
            String result = adminService.updateUsers(dest);

            dest.delete();

            if (result.equals("success")) {
                map.put("message", "최신기수 업로드가 완료되었습니다.");
            } else {
                map.put("message", "새로 업로드된 데이터가 없습니다. 파일을 다시 확인해주세요.");
            }
        }catch (NullPointerException e){
            map.put("message", "파일을 선택해주세요.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "기수별 층수 보기")
    @GetMapping("/view/class&floor")
    public ResponseEntity<Map<String, Object>> getClassOfFloor(){
        Map<String, Object> map = new HashMap<>();
        List<ClassFloorDto> result =  adminMapper.getClassOfFloor();

        if(!result.isEmpty()) {
            map.put("floorData", floor);
            map.put("ClassOfFloorData", result);
        }

        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "기수별 층수 데이터 바꾸기", notes = "기수가 0으로 들어올 경우 fail 처리")
    @PostMapping("/change/floor")
    public ResponseEntity<Map<String, Object>> changeFloor(@RequestBody ClassFloorDto classFloorDto){
        Map<String, Object> map = new HashMap<>();
        String result;

        if(floor.contains(classFloorDto.getFloor())){
            result = adminService.updateClassOfFloorData(classFloorDto);
        }else{
            result = "fail";
        }

        if(result.equals("success")){
            System.out.println(classFloorDto.getClasses() + "기 가 " + classFloorDto.getFloor() + "로 변경되었음.");
            map.put("message", "층수 변경이 완료되었습니다!");
        }else{
            map.put("message", "층수 변경 실패");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "회의실 데이터 조회", notes = "floor가 2,3,4가 아닌경우 fail 처리")
    @GetMapping("/view/room")
    public ResponseEntity<Map<String, Object>> getRoomData(@RequestParam("floor")int floor){
        Map<String, Object> map = new HashMap<>();
        List<Room> result = adminMapper.getFloorRoomData(floor);
            if(!result.isEmpty()){
                map.put("RoomData", result);
            }else{
                map.put("message", "회의실 데이터 불러오기 실패");
            }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value = "회의실별 데이터 수정", notes = "floor가 2,3,4가  아닌경우 변경불가 메시지")
    @PostMapping("/update/room-data")
    public ResponseEntity<Map<String, Object>> updateRoomTIme(@RequestBody RoomDto roomDto){
        Map<String, Object> map = new HashMap<>();
        System.out.println(roomDto);
        int result = adminMapper.updateRoomData(roomDto.getMaxTime(), roomDto.getRoomType(),roomDto.getRoomId());
        if (result != 0) {
            map.put("message", "회의실 데이터가 수정되었습니다.");
        } else {
            map.put("message", "회의실 데이터 변경 실패");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


    @ApiOperation(value = "전체 기수리스트 보내기")
    @GetMapping("/view/class-list")
    public ResponseEntity<Map<String, Object>> getAllClassList(){
        Map<String, Object> map = new HashMap<>();
        List<Integer> classListResult = adminMapper.getAllClass();
        map.put("ClassList", classListResult);
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value="기수별 유저데이터 보내기")
    @GetMapping("/view/user")
    public ResponseEntity<Map<String, Object>> getAllUserData(@RequestParam("classes") int classes) {
        Map<String, Object> map = new HashMap<>();
        List<UserDto> result = adminMapper.allSelectUser(classes);
        map.put("AllUserData", result);
       return ResponseEntity.status(HttpStatus.OK).body(map);
    }

    @ApiOperation(value="유저 데이터 수정")
    @PostMapping("/update/user")
    public ResponseEntity<Map<String, Object>> updateUserData(@RequestBody UserDto userDto){
        Map<String, Object> map = new HashMap<>();
        System.out.println(userDto);
        int result = adminMapper.updateUserData(userDto.getUserName(), userDto.getRoles(), userDto.getFloor(), userDto.getUserId());
        if(result == 1){
            map.put("message", userDto.getUserName() + "님의 데이터가 수정되었습니다.");
        }else{
            map.put("message", "데이터 업데이트 실패");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


    @ApiOperation(value = "유저 데이터 삽입")
    @PostMapping("insertion/user")
    public ResponseEntity<Map<String, Object>> insertUserData(@RequestBody UserDto userDto) {
        Map<String, Object> map = new HashMap<>();
        System.out.println(userDto);
        try {
            if(!userDto.getUserId().equals(" ") || !userDto.getUserName().equals(" ") ) {
                int result = adminMapper.insertUserData(userDto.getClasses(), userDto.getUserId(), userDto.getUserName(), userDto.getRoles(), userDto.getFloor());
                if (result == 1) {
                    map.put("message", userDto.getUserName() + "님의 데이터가 추가되었습니다.");
                } else {
                    map.put("message", "빈 값을 입력해주세요!");
                }
            }
        }catch(NullPointerException e) {
            System.out.println(e);
            map.put("message", "빈 값을 입력해주세요!");
        }catch(Exception e){
            System.out.println(e);
            map.put("message", "이미 있는 인재번호입니다.");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


    @Data
    static class UserIdList{
        private List<String> userIdList;
    }

    @ApiOperation(value="유저 데이터 삭제")
    @PostMapping("/deletion/user")
    public ResponseEntity<Map<String, Object>> deleteUserData(@RequestBody UserIdList userIdList){
        Map<String, Object> map = new HashMap<>();

        try {
            System.out.println(userIdList);
            List<String> usersId = userIdList.userIdList;
            System.out.println(usersId);
            String usersIdString = userService.changeString(null, usersId);
            int result = adminMapper.deleteUser(usersIdString);
            if (result >= 1) {
                map.put("message", "데이터 삭제가 완료되었습니다.");
            } else {
                map.put("message", "삭제할 데이터를 체크 해 주세요!");
            }
        }catch (NullPointerException e){
            map.put("message", "삭제할 데이터를 체크 해 주세요!");
        }catch (StringIndexOutOfBoundsException e){
            map.put("message", "삭제할 데이터를 체크 해 주세요!");
        }
        return ResponseEntity.status(HttpStatus.OK).body(map);

    }

    @ApiOperation(value = "로그아웃")
    @GetMapping(value = "/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request){
        Map<String, String> map = new HashMap<>();
        map.put("message", "success");
        return ResponseEntity.status(HttpStatus.OK).body(map);
    }


}
