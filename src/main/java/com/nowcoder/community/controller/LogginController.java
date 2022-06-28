package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author guofan
 * @Date 2022-05-24 16:37
 * @Description
 */

@Controller
public class LogginController implements CommunityConstant{

    //创建日志
    private static final Logger logger = LoggerFactory.getLogger(LogginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    //跳转注册页面
    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    //跳转登录页面
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if (map==null||map.isEmpty()){
            model.addAttribute("msg","注册成功！我们已经向您的邮箱发送一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    //http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,@PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if (result==ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功！您的账号可以正常使用了！");
            model.addAttribute("target","/login");
        }else if (result==ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作！您的账号已经激活过！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败！您提供的激活码无效！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        //根据配置类里写的配置，生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //把生成的验证码文字存入session，好在后面的请求中使用
        //session.setAttribute("kaptcha",text);

        //验证码的归属,要发给客户端，客户端要发给cookie保存
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
        cookie.setMaxAge(60);//生存时间60s
        cookie.setPath(contextPath);//有效路径设置为整个项目下都有效
        response.addCookie(cookie);//把cookie添加到response里，即为发送给客户端了
        //将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);//生成key
        redisTemplate.opsForValue().set(redisKey,text,60, TimeUnit.SECONDS);//把验证码存到Redis里，设置有效时间60s

        //图片直接输出给浏览器
        //声明返回的数据类型：png类型的图片
        response.setContentType("image/png");

        try {
            //从response里获取输出流
            OutputStream os = response.getOutputStream();
            //用工具ImageIO进行输出
            ImageIO.write(image,"png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败："+e.getMessage());
        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    public String login(String username,String password,String code,boolean rememberme,
                        Model model,/*HttpSession session,*/HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner){
        //先看验证码对不对，验证码不对的话，账号密码直接不用看了
        //String kaptcha = (String) session.getAttribute("kaptcha");

        //从redis里取
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner/*说明凭证在*/)){
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);//拼key
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);//从Redis取值
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg","验证码错误！");
            return "/site/login";
        }

        //验证码正确了，检查账号密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,expiredSeconds);
        if (map.containsKey("ticket")){
            //把凭证存到cookie发送给客户端
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            //进行cookie的相关设置
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
                //发送给客户端
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout",method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        //重定向默认就是get请求
        return "redirect:/login";
    }
}
