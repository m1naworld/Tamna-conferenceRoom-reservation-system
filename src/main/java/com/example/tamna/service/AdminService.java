package com.example.tamna.service;

import com.example.tamna.dto.ClassFloorDto;
import com.example.tamna.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;


    // 최신 인재들 업데이트
    public String updateUser(File dest) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(dest));
        String line;
        int result = 0;
        if((line= br.readLine())!=null) {
            while ((line = br.readLine()) != null) {
                String[] datalines = line.split(",");
                try {
                    int classes = Integer.parseInt(datalines[0]);
                    String userId = datalines[1];
                    String name = datalines[2];
                    System.out.println(classes + " " + userId + " " + name);
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
