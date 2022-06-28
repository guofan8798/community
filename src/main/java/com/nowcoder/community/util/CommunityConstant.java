package com.nowcoder.community.util;

/**
 * @Author guofan
 * @Date 2022-05-25 15:26
 * @Description
 */
public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认的登录凭证超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态下的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 30;

    /**
     * 实体类型：1.帖子
     *          2.评论
     *          3.用户
     */
    int ENTITY_TYPE_POST = 1;
    int ENTITY_TYPE_COMMENT = 2;
    int ENTITY_TYPE_USER = 3;

    /**
     * 事件主题：
     *      1.评论
     *      2.点赞
     *      3.关注
     */
    String TOPIC_COMMENT = "comment";
    String TOPIC_LIKE = "like";
    String TOPIC_FOLLOW = "follow";

    /**
     * 系统用户id
     */
    int SYSTEM_USER_ID = 1;
}
