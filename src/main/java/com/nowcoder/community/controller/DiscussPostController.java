package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * @Author guofan
 * @Date 2022-06-01 22:10
 * @Description 帖子相关业务
 */

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    /**
     * 其中，帖子的其他属性：
     * id          自动生成
     * userId      从hostHolder里查到用户，然后getUserId
     * title       用户自己写
     * content     用户自己写
     * createTime  我们 new
     * type        默认 0
     * status      默认
     *
     * @param title   用户自己写的
     * @param content 用户自己写的
     * @return 帖子对象
     */
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还未登录！");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());

        discussPostService.addDiscussPost(post);

        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    /**
     * 帖子详情页面
     *
     * @param discussPostId
     * @param model
     * @return
     */
    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //查询帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        //①把帖子的数据添加到模板
        model.addAttribute("post", post);
        //根据帖子id查到用户(因为不止要查到帖子，还要展示用户头像，用户id等信息)
        //得到帖子的作者
        User user = userService.findUserById(post.getUserId());
        //②把作者的信息传给模板
        model.addAttribute("user", user);
        //点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        //评论：给帖子的评论
        //回复：给回复的评论
        //！！！评论列表！！！
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        //Vo ---> view object 遍历这个list，每遍历一次，就得到一个评论，然后创建一个map，把评论和用户都封装进去(关联起来)。
        //评论的VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                //一个评论的VO
                Map<String, Object> commentVo = new HashMap<>();
                //往VO里添加评论
                commentVo.put("comment", comment);
                //往VO里添加作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                //点赞数量
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount",likeCount);
                //点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus",likeStatus);

                //！！！回复列表！！！
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                //回复的VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        //存回复
                        replyVo.put("reply", reply);
                        //存作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));

                        //回复的目标
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        //点赞数量
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount",likeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus",likeStatus);

                        //把所有的replyVo封装起来装进replyVoList里
                        replyVoList.add(replyVo);
                    }
                }
                //commentVo返回给页面，页面通过它获取到要展现的一切数据
                commentVo.put("replys", replyVoList);

                //commentVO里加上回复的数量
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                //最后把所有的commentVo加到commentVoList里
                commentVoList.add(commentVo);
            }
        }

        //将commentVoList加入到视图
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }


}
