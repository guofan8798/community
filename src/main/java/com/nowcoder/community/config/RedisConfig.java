package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @Author guofan
 * @Date 2022-06-10 20:53
 * @Description Redis的配置类.(通过 RedisTemplate 来访问 Redis)
 */

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //设置序列化方式，就是定义数据转换格式：key转成string类型，value(可能是各种类型)转成json类型
        //设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置非哈希value的序列化方式
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //让以上设置生效
        template.afterPropertiesSet();

        return template;
    }
}
