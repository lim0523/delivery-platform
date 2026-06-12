package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface UserMapper {
    @Select("select * from user where id=#{userId}")
     User getById(Long userId);

    @Select("select * from user where openid =#{openid}")
    User getByOpenid(String openid);

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user(openid, name, phone, sex, id_number, avatar, create_time) " +
            "values " +
            "(#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    void insert(User user);


    Integer getByCreateTime(LocalDateTime beginDate, LocalDateTime endDate);
}
