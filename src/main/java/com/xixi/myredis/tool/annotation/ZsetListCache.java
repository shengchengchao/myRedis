package com.xixi.myredis.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/26
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ZsetListCache {

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


}
