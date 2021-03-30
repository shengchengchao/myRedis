package com.xixi.myredis.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shengchengchao
 * @Description 防重复提交
 * @createTime 2021/3/29
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RequestProtect {
    /**
     * 字段
     * @return
     */
    String body() default "";


    /**
     * 重复过期时间
     * @return
     */
    int expire() default 3;


    String tokenField() default "";
}
