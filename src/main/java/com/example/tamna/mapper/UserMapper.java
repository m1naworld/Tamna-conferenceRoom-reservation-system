package com.example.tamna.mapper;

import com.example.tamna.model.UserDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    // 유저 아이디로 데이터 검색
    @Select("SELECT * FROM USER WHERE user_id=#{userId}")
    UserDto findByUserId(String userId);

    // 유저들 이름으로 select -> 이름은 다른 기수와 겹칠 것을 대비해 같은 기수에서만 찾음
    @Select("SELECT * FROM USER WHERE classes=#{classes} AND user_name IN (${users})")
    List<UserDto>  findByUserName(@Param("classes")int classes, @Param("users") String users);

    // 기수별 유저 이름들만 가져오기
    @Select("SELECT USER_NAME FROM USER WHERE classes=#{classes} ORDER BY USER_NAME ASC")
    List<String> findUserNamesByClasses(int classes);

}
