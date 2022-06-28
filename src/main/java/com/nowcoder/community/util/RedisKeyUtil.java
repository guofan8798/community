package com.nowcoder.community.util;

/**
 * @Author guofan
 * @Date 2022-06-11 16:20
 * @Description 生成Key的工具
 */
public class RedisKeyUtil {

    //key中有冒号，将它定义为常量，方便拼
    private static final String SPLIT = ":";
    //声明赞以这个形式为前缀
    private static final String PREFIX__ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";


    //生成某个实体的赞的key
    //赞的key ---> like:entity:entityType:entityId
    //赞的value ---> set (存的是userId。谁给实体点了赞，就把这个userId存到set里，方便后续功能开发)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX__ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    //生成某个用户的赞的key
    //赞的key ---> like:user:userId
    //赞的value ---> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体。followee：userId：entityType ---> zset（entityId，now）
     * now:是当前时间的毫秒数。作为score，方便以后进行排序
     *
     * @param userId     谁关注了这个实体？
     * @param entityType 这个实体的类型
     * @return key名字
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝。follower：entityType：entityId ---> zset（userId，now）
     *
     * @param entityType 该实体的实体类型
     * @param entityId   该实体的实体id
     * @return key名字
     */
    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 拼验证码的key
     *
     * @param owner 验证码的拥有者，用户的临时凭证(是一段字符串)
     * @return
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }
}
