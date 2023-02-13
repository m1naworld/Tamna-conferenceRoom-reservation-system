package com.example.tamna.service;

import com.example.tamna.config.jwt.JwtProvider;
import com.example.tamna.dto.ClassFloorDto;
import com.example.tamna.mapper.AdminMapper;
import com.example.tamna.mapper.UserMapper;
import com.example.tamna.model.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;
    private final UserMapper userMapper;
    private final JwtProvider jwtProvider;


    // 어드민 로그인
    public String  getAdminToken(String userId){
        UserDto user = userMapper.findByUserId(userId);

        try{
            assert user != null;
            if(user.getRoles().equals("ADMIN")){
                return jwtProvider.createAccessToken(userId);
            }
            return "fail";
        }catch (NullPointerException e){
            return "fail";
        }
    }


    public String updateUsers(File dest) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dest), "euc-kr"));
        String line;
        int result = 0;

        if((line= br.readLine())!=null) {
            while ((line = br.readLine()) != null) {
                String[] datalines = line.split(",");
                try {
                    int classes = Integer.parseInt(datalines[0]);
                    String userId = datalines[1];
                    String name = datalines[2];
                    result = adminMapper.insertUser(classes, userId, name);
                } catch (NumberFormatException e) {
                    continue;
                }
            }
            br.close();

            if(result >= 1){
                return "success";
            }
        }
        return "fail";

    }

    // 기수별 층수 데이터 수정
    public String updateClassOfFloorData(ClassFloorDto classFloorDto){
        if(classFloorDto.getClasses()!= 0) {
            int result = adminMapper.updateFloor(classFloorDto.getFloor(), classFloorDto.getClasses());
            if(result > 0){
                return "success";
            }
        }
        return "fail";
    }



}
