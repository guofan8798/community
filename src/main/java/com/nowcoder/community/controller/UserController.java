package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 点击链接，跳转到修改用户信息的页面
 *
 * @Author guofan
 * @Date 2022-05-31 17:18
 * @Description
 */

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     * 用户点击链接，跳转到修改页面
     *
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /**
     * 具体的上传步骤的代码实现(用户填写信息之后点击提交的逻辑)
     *
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片！");
            return "/site/setting";
        }

        //获取后缀，看是否合理
        String fileName = headerImage.getOriginalFilename();
        //截取最后一个“.”后面的内容
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //如果后缀是空的，给个提示
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确！");
            return "/site/setting";
        }

        //后缀合理，生成一个随机字符串(文件名)+后缀(比如png)
        fileName = CommunityUtil.generateUUID() + suffix;
        //确定文件存放的路径(application.properties里配置的)
        File dest = new File(uploadPath + "/" + fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败！" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常！", e);
        }

        //存成功了，就要更新当前用户的头像路径(web访问路径)
        //比如：http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /**
     * 获取用户头像
     */
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        //服务器存放的路径
        fileName = uploadPath + "/" + fileName;
        //声明图片的后缀，解析，输出到浏览器
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //响应图片
        response.setContentType("image/" + suffix);
        //图片是二进制数据，需要用到字节流
        try (
                //先创建文件的输入流，用来读取文件。读了才能输出
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            //先设置一个缓冲区，不是边输入边输出
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                //！=-1就是读到数据了，=-1就是没有读到
                os.write(buffer, 0, b);
            }

        } catch (IOException e) {
            logger.error("读取头像失败！" + e.getMessage());
        }
    }

    /**
     * 开发个人主页，显示该用户的获赞数量。
     * @param userId 要查的用户id
     * @param model 视图
     * @return profile:主页
     */
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)

    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }

        //查询用户，把用户传给页面
        model.addAttribute("user",user);
        //查询获赞数量，把该用户的获赞的数量传给页面
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);
        //查询关注的目标数量
        long followeeCount = followService.findFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.fingFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注(当前登录用户对这个用户是否已关注)
        boolean hasFollowed = false;
        if (hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }

}
