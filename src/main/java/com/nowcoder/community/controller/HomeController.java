package com.nowcoder.community.controller;

import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author guofan
 * @Date 2022-05-19 16:12
 * @Description
 */

@Controller
public class HomeController implements CommunityConstant{

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    @RequestMapping(path = "/index",method = RequestMethod.GET)
    //这里的返回的String是视图的名字
    public String getIndexPage(Model model, Page page){
        //方法调用之前，SpringMVC会自动实例化model和page，并将page注入到model
        //所以在Thymeleaf模板中可以直接访问page对象中的数据

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        //list里放的是通过userIs查找到的帖子
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit());
        //创建map的集合discussPosts，用来装查找之后的帖子和用户
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (list!=null){
            //遍历
            for (DiscussPost post:list){
                Map<String,Object> map = new HashMap<>();
            //往map里装帖子
                map.put("post",post);
            //往map里装用户
                //帖子里的user_id就对应用户里的id，再根据id可以获得username。
                //(这样就将两张表关联起来了，就能获得我们需要的userName属性)
                User user = userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likeCount",likeCount);

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }

    /**
     * 到500的路径
     * @return
     */
    @RequestMapping(path = "/error",method = RequestMethod.GET)
    public String getErrorPage(){
        return "/error/500";
    }
}
