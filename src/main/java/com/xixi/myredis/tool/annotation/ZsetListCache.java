package com.xixi.myredis.tool.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author shengchengchao
 * @Description 该注解主要是用于数据的分页，使用zset来进行数据的存储，
 * 目前仍然存在一个问题 如果redis和数据库存在不同步的情况，无法进行及时的处理 需要保证来数据进入到数据库的同时，同时能够加入到redis中
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
