package com.nowcoder.community.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author guofan
 * @Date 2022-05-31 21:57
 * @Description
 */

@Target(ElementType.METHOD)
//运行时有效
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginRequired {

}
