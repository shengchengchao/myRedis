package com.xixi.myredis.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisSimpleCache {

    /**
     * 过期时间 单位为秒
     * @return
     */
    int expire() default 86400;

    /**
     * redis 的key
     * @return
     */
    String redisKey() default "";

    /**
     * 在请求后是否刷新过期时间  true为刷新 false 为不刷新
     * @return
     */
    boolean reCalFlag() default false;
}
