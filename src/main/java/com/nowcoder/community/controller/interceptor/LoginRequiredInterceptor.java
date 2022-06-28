package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * @Author guofan
 * @Date 2022-05-31 22:02
 * @Description
 */

//@component：标注一个类为Spring容器的Bean
@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        //先判断这个Object handler 是不是方法，因为我们拦截的是方法
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            //取这个类型的注解
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class);
            //loginRequired != null，取到了这个注解，说明这个方法是需要登录才能访问的
            //hostHolder.getUser()==null没有获取到用户，说明未登录
            if (loginRequired != null && hostHolder.getUser() == null) {
                try {
                    response.sendRedirect(request.getContextPath() + "/login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;//拒绝后续的请求
            }
        }

        return true;
    }
}
