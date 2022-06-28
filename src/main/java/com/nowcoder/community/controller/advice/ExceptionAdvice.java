package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @Author guofan
 * @Date 2022-06-09 15:57
 * @Description advice 通知
 */

/**
 * 统一异常处理
 * 定义一个控制器通知组件，处理所有Controller所发生的异常
 *
 * 统一异常处理：
 *      异步请求返回json错误提示；
 *      普通异常返回HTML错误页面。
 * @ControllerAdvice 表示该类是Controller的全局配置类
 * annotations = Controller.class ---> 只去扫描带有@Controller注解的那些bean
 */
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }

        //判断你是普通请求还是异步请求:普通请求返回HTML；异步请求需要返回json
        String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)){
            //说明这是一个异步请求，需要响应一个json
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        }else {
            //说明是普通请求，需要重定向到错误页面
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }
}
