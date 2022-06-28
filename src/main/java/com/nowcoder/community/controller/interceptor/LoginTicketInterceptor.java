package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @Author guofan
 * @Date 2022-05-31 16:19
 * @Description
 */

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 在请求开始之初，通过凭证找到了用户，并且把用户暂存到hostHolder里(暂存到线程对应的对象里)
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //  从cookie中获取ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            //  查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //  检查凭证是否有效(过期没)
            //  loginTicket.getExpired().after(new Date() 超时时间晚于当前时间
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //  根据凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //  在本次请求中持有用户(暂存起来)
                hostHolder.setUser(user);
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        //得到当前线程持有的User
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        hostHolder.clear();
    }
}
