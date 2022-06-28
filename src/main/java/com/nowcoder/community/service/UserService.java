package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Author guofan
 * @Date 2022-05-19 16:00
 * @Description
 */

@Service
public class UserService implements CommunityConstant {

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 根据id查user，使用了缓存
     * @param id
     * @return
     */
    public User findUserById(int id) {
//        return userMapper.selectByID(id);
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * 注册方法
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //验证参数是否重复(username,email等)
        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //将原来的密码加上salt的随机字符串(截取五位)，进行md5加密，对原密码进行覆盖
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));

        //注册的用户都是普通用户
        user.setType(0);
        //0是未激活，需要激活码才能激活
        user.setStatus(0);
        //设置随机激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        //设置随机头像路径
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        Context context = new Context();
        //给用户发送激活邮件
        context.setVariable("email", user.getEmail());
        //激活路径示例：http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        //如果没有问题，map里就是空的
        return map;
    }

    /**
     * 激活方法
     *
     * @param userId 用户id
     * @param code   激活码
     * @return
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectByID(userId);

        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);//修改了之后，要清理缓存
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值的处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //账号密码都不为空，则要看是否合法
        //验证账号是否瞎写的
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账号不存在！");
            return map;
        }

        //验证账号是否被激活
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活！");
            return map;
        }

        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());

        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误！");
            return map;
        }

        //满足以上条件，说明账号密码都正确，满足登录条件，生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        //把生成的登录凭证存到Redis里
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());//拼key
        redisTemplate.opsForValue().set(redisKey, loginTicket);//会把对象loginTicket序列化为一个JSON格式字符串

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /**
     * 退出功能：把状态改为 1
     *
     * @param ticket
     */
    public void logout(String ticket) {
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);//拼key
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);//从Redis里取到key对应的值，将object类型强转为loginTicket类型
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);//新值(将status改为了1)覆盖旧值
    }

    /**
     * 根据凭证查询对应的记录
     *
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket) {
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);//拼key
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 修改用户头像路径
     *
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
//        return userMapper.updateHeader(userId, headerUrl);
        int rows = userMapper.updateHeader(userId, headerUrl);//更新以后返回行数
        clearCache(userId);//清理缓存
        return rows;
    }

    /**
     * 根据用户名查找用户
     *
     * @param username 传入的用户名
     * @return 查到的用户
     */
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    //1.优先从缓存中取数据
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);//拼key
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    //2.取不到时初始化缓存数据(往缓存中存数据)
    private User initCache(int userId) {
        User user = userMapper.selectByID(userId);//先从MySQL里把数据查到
        String redisKey = RedisKeyUtil.getUserKey(userId);//拼key
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);//查到的user装到Redis里，(因为是缓存，所以要有过期时间)设置过期时间1h
        return user;
    }

    //3.数据变更时，需要清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);//拼key
        redisTemplate.delete(redisKey);//直接把key删掉
    }
}
