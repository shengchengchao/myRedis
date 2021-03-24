package com.xixi.myredis.tool.service;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/24
 */

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.xixi.myredis.tool.annotation.RedisParamKey;
import com.xixi.myredis.tool.annotation.RedisSimpleCache;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TestService {



    @RedisSimpleCache(redisKey = "test",expire = 100)
    public String test(@RedisParamKey String k1 ){
        System.out.println("进入查询");
        return "aaa";
    }

    @RedisSimpleCache(redisKey = "test2",expire = 100)
    public Integer test2(@RedisParamKey String k2 ){
        System.out.println("进入查询");
        return 2;
    }
    @RedisSimpleCache(redisKey = "test3",expire = 100)
    public Double test3(@RedisParamKey String k3 ){

        System.out.println("进入查询");
        return 2.0;
    }

    @RedisSimpleCache(redisKey = "test4",expire = 100)
    public List<String> test4(@RedisParamKey String k4 ){
        System.out.println("进入查询");
        return Lists.newArrayList("1","2");
    }

    @RedisSimpleCache(redisKey = "test5",expire = 100)
    public Map<String,String> test5(@RedisParamKey Integer k5 ){
        System.out.println("进入查询");
        Map<String, String> map  = new HashMap<>();
        map.put("1","2");
        return map;
    }
    @RedisSimpleCache(redisKey = "test6",expire = 100)
    public Set<String> test6(@RedisParamKey String k6 ){
        System.out.println("进入查询");
        return Sets.newHashSet("1","2");
    }

    @RedisSimpleCache(redisKey = "test7",expire = 100)
    public Object test7(@RedisParamKey String k7 ){
        System.out.println("进入查询");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("11","23");
        return jsonObject;
    }

    @RedisSimpleCache(redisKey = "test8",expire = 100)
    public Object test8(@RedisParamKey Object o ){
        System.out.println("进入查询");
        JSONObject jsonObject = (JSONObject)o;
        jsonObject.put("11","23");
        jsonObject.put("12","22");
        return jsonObject;
    }
}
