package com.nowcoder.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author guofan
 * @Date 2022-06-14 21:28
 * @Description 点赞、关注、评论都是一个时间
 */
public class Event {

    private String topic;//kafka角度叫主题，其实就是事件类型(是点赞？关注？评论？)
    private int userId;//触发事件的人(张三给李四点赞，张三[userId]触发事件，给李四[entityUserId]发通知)
    private int entityType;//事件对应的实体
    private int entityId;//事件对应的实体id
    private int entityUserId;//实体对应的作者的id(张三给李四点赞，张三[userId]触发事件，给李四[entityUserId]发通知)
    private Map<String,Object> data = new HashMap<>();//方便以后扩展，存放其他的数据

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }

}
