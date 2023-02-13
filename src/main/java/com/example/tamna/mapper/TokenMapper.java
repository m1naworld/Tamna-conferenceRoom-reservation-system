package com.example.tamna.mapper;

import com.example.tamna.model.Token;
import org.apache.ibatis.annotations.*;

import java.sql.Date;

@Mapper
public interface TokenMapper {

    @Insert("INSERT INTO TOKEN(DATES, USER_ID, REFRESH_TOKEN) VALUES(#{today}, #{userId}, #{refreshToken})")
    int insertToken(@Param("today") Date today, @Param("userId") String userId, @Param("refreshToken") String refreshToken);

    @Select("SELECT * FROM TOKEN WHERE REFRESH_TOKEN=#{refreshToken}")
    Token findToken(String refreshToken);

    @Delete("DELETE FROM TOKEN WHERE REFRESH_TOKEN=#{refreshToken}")
    int deleteToken(String refreshToken);

}
