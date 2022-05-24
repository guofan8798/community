package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author guofan
 * @Date 2022-05-19 14:40
 * @Description
 */

@Mapper
public interface DiscussPostMapper {
    //用于后续支持分页：offset 每一页起始行的行号；limit每一页最多显示多少条数据
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    //查询帖子一共有多少个，为了后续分页看分多少页
    int selectDiscussPostRows(@Param("userId") int userId);
}
