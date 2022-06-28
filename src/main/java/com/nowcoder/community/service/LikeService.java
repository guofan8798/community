package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * @Author guofan
 * @Date 2022-06-11 16:31
 * @Description
 */

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞功能，一次点赞，两次取消
     *
     * @param userId        当前用户
     * @param entityType    实体类型
     * @param entityId      实体id
     * @param entityUserId  实体所属用户的userId
     * @param entityLikeKey 某个实体的赞的key
     * @param userLikeKey   某个用户的赞的key
     */
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

                //当前用户有没有对当前实体点过赞
                //此方法是查询，在Redis里，查询要放在事务外，否则不显示(因为Redis是一次执行所有操作，食物里的查询可能不生效)
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();

                if (isMember) {
                    //当前用户点过赞，点了之后就是取消点赞，则需将该用户的userId从set中移除
                    operations.opsForSet().remove(entityLikeKey, userId);
                    //实体所属的user收到的赞要-1
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //当前用户没点过赞，点了之后就是进行点赞，需要将该用户的userId加入set
                    operations.opsForSet().add(entityLikeKey, userId);
                    //实体所属的user收到的赞+1
                    operations.opsForValue().increment(userLikeKey);
                }

                //执行事务
                return operations.exec();
            }
        });
    }

    /**
     * 查询某个实体类型点赞的数量
     *
     * @param entityType 要查询的实体的类型
     * @param entityId   要查询的实体的id
     * @return 查询到的set(userId)里的数据的数量--->即所有点过赞的userId的数量
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //统计entityLikeKey这个key对应的set里面的数据的数量
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询谋和用户有没有对某个实体点过赞
     *
     * @param userId     要查询的用户
     * @param entityType 实体类型
     * @param entityId   实体id
     * @return 可以返回 boolean值，点过返回true，没点过返回false；
     * 但是为了拓展以后的功能：“踩”，所以返回int类型 ---> 返回-1表示“踩”。
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    //查询某个用户获得的赞的数量
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        //count可能为0，不为零的话则显示他的整数形式
        return count == null ? 0 : count.intValue();
    }
}
