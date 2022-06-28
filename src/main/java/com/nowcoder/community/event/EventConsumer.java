package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author guofan
 * @Date 2022-06-14 21:50
 * @Description 处理事件
 */

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;//往message表里插入数据，需要依赖 messageService

    //定义消费者方法
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {//即便是公共的组件也要判断一下，严谨
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);//将JSON格式字符字符串转换成对象，类型为Event
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        //发送站内通知(构造一个message)
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        //构造map，在事件触发时，存相关数据
        //通知页要展示很多信息，是一个JSON字符串，所以message表里存一个对象(JSON字符串)
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());//触发事件的人
        content.put("entityType", event.getEntityType());//对哪个实体作了处理
        content.put("entityId", event.getEntityId());//具体实体的id

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());//把event的map(名为data)数据也存到content
            }
        }

        //把content(上面的map)对象转换为JSON字符串，存到message的content字段
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }
}
