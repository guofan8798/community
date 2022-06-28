package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @Author guofan
 * @Date 2022-06-07 21:42
 * @Description
 */

@Controller
@RequestMapping(path = "/comment")
public class CommentController implements CommunityConstant{

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(path = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        //表示有效
        comment.setStatus(0);
        comment.setCreateTime(new Date());

        commentService.addComment(comment);

        //触发评论事件(消费该事件所需都放进该event里了)
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId",discussPostId);

        if (comment.getEntityType() == ENTITY_TYPE_POST){//假如评论的目标是帖子
            DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());//先查到帖子
            event.setEntityUserId(target.getUserId());//设置event的字段
        }else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){//如果评论的目标是评论
            Comment target = commentService.findCommentById(comment.getEntityId());//评论的实体id就是我的目标
            event.setEntityUserId(target.getUserId());
        }

        //调producer发布消息
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
