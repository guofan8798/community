package com.nowcoder.community.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author guofan
 * @Date 2022-06-09 19:29
 * @Description 定义一个切面组件，在用户访问任何业务组件之前，记录本次访问的日志
 */

@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);

    //声明切点
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut(){

    }

    //前置通知(为了在一开头织入程序)
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //声明要记录的日志的格式：[用户1.2.3.4]在[xx时间xx],访问了[com.nowcoder.community.service.xxx(方法)]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes==null){//这是一个特殊的调用，不是常规的页面的调用，这个时候就不去记录日志了
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        //获得ip地址
        String ip = request.getRemoteHost();
        //获得时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //获得目标的类型名和方法名  joinPoint.getSignature().getDeclaringTypeName() ---> com.nowcoder.community.service
        //                        joinPoint.getSignature().getName() ---> xxx(方法)
        String target = joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();

        logger.info(String.format("用户[%s],在[%s],访问了[%s].",ip,now,target));
    }
}
