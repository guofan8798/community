package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author guofan
 * @Date 2022-06-06 20:44
 * @Description
 */

@Mapper
public interface CommentMapper {

    /**
     * 分页功能
     * @param entityType 实体类型(帖子？人家的评论？)
     * @param entityId 帖子的话，那一个帖子？
     * @param offset 偏移量
     * @param limit 每页显示条数
     * @return 所有评论，并分页
     */
    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId,@Param("offset") int offset,@Param("limit") int limit);

    /**
     * 查询一共有多少条数据
     * @param entityType 实体类型(帖子？人家的评论？)
     * @param entityId 帖子的话，那一个帖子？
     * @return 所有评论数
     */
    int selectCountByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId);

    /**
     * 添加评论
     * @param comment 评论(是一个实体)
     * @return 添加了几行？
     */
    int insertComment(Comment comment);

    /**
     * 根据 id 查一个comment
     * @param id
     * @return
     */
    Comment selectCommentById(int id);
}
