package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Author guofan
 * @Date 2022-06-08 15:37
 * @Description
 */

@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表，针对每个用户，只返回最近的一条私信
     *
     * @param userId 当前用户id
     * @param offset 偏移量
     * @param limit  每页显示量
     * @return 会话(消息)列表
     */
    List<Message> selectConversations(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询当前用户的会话数量
     *
     * @param userId 当前用户id
     * @return 总的会话数量
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话包含的私信列表
     *
     * @param conversationId 会话id
     * @return 该会话列表中的所有消息
     */
    List<Message> selectLetters(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询某个会话包含的私信数量
     *
     * @param conversationId 会话id
     * @return 该会话列表中的所有消息数量
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询未读的私信数量
     *
     * @param userId         当前用户id
     * @param conversationId 会话id
     * @return 所有未读私信数
     * 注：区分总的未读数，和与其他的用用户之间的未读数。都要显示出来。
     * conversationId是动态拼的，不一定有
     */
    int selectLetterUnreadCount(@Param("userId") int userId, @Param("conversationId") String conversationId);

    /**
     * 新增消息
     *
     * @param message 实体
     * @return 添加行数
     */
    int insertMessage(Message message);

    /**
     * 修改消息的状态，将未读变为已读
     *
     * @param ids    消息集合(消息页面含有多条未读消息，是集合)
     * @param status 消息的状态
     * @return 更新行数
     */
    int updateStatus(@Param("ids") List<Integer> ids, @Param("status") int status);

    /**
     * 查询某个主题下最新的通知
     * @param userId 查谁
     * @param topic 哪个主题下(关注？评论？点赞)？
     * @return 最新通知
     */
    Message selectLatestNotice(@Param("userId") int userId,@Param("topic") String topic);

    /**
     * 查询某个主题下所包含的通知的数量
     * @param userId 查谁
     * @param topic 哪个主题
     * @return 该主题下的通知数量
     */
    int selectNoticeCount(@Param("userId") int userId,@Param("topic") String topic);

    /**
     * 查询未读的通知的数量
     * @param userId 查谁
     * @param topic 主题
     * @return 未读消息数(需要筛选状态)
     */
    int selectNoticeUnreadCount(@Param("userId") int userId,@Param("topic") String topic);

    /**
     * 查询某个主题所包含的通知列表
     * @param userId 查谁
     * @param topic 哪个主题
     * @param offset 分页
     * @param limit 分页
     * @return 通知的集合
     */
    List<Message> selectNotices(@Param("userId") int userId,@Param("topic") String topic,@Param("offset") int offset,@Param("limit") int limit);
}
