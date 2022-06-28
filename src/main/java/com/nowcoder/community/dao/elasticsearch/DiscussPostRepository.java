package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author guofan
 * @Date 2022-06-23 16:30
 * @Description
 */

//spring提供的，针对数据访问层的注解
//不是mapper，mapper是mybatis专有的注解
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer>{
}
