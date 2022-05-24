package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @Author guofan
 * @Date 2022-05-19 12:19
 * @Description
 */

@Mapper
public interface UserMapper {

    User selectByID(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(@Param("id") int id,@Param("status") int status);

    int updateHeader(@Param("id") int id,@Param("headerUrl") String headerUrl);

    int updatePassword(@Param("id") int id,@Param("password") String password);

}
