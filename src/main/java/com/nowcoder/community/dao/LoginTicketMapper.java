package com.nowcoder.community.dao;

import com.nowcoder.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

/**
 * @Author guofan
 * @Date 2022-05-26 17:28
 * @Description
 */

//@Mapper 这是一个数据访问对象，需要由容器来管理
@Mapper
@Deprecated
public interface LoginTicketMapper {

    //添加凭证
    @Insert({
            "insert into login_ticket (user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    //声明id字段自动生成
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    //根据凭证查询到整条记录
    @Select({
            "select id,user_id,ticket,status,expired ",
            "from login_ticket where ticket = #{ticket} "
    })
    LoginTicket selectByTicket(String ticket);

    //方便后续对用户状态(根据传入的凭证)进行更改
    @Update({
            "update login_ticket set status = #{status} where ticket = #{ticket}"
    })
    int updateStatus(@Param("ticket") String ticket,@Param("status") int status);
}
