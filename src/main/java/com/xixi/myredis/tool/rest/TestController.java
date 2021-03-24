package com.xixi.myredis.tool.rest;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xixi.myredis.tool.annotation.RedisParamKey;
import com.xixi.myredis.tool.annotation.RedisSimpleCache;
import com.xixi.myredis.tool.base.BaseResult;
import com.xixi.myredis.tool.service.TestService;
import com.xixi.myredis.tool.util.MyFastJsonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/16
 */
@Api(tags = "测试")
@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @Autowired
    private RedisTemplate redisTemplate;


    @Autowired
    private TestService testService;

    @ApiOperation(value = "测试redis 注解 RedisSimpleCache")
    @GetMapping("/redis")
    @ResponseBody
    public BaseResult testData(@RequestParam String body) {
        switch (body){
            case "1" :
                System.out.println(testService.test("k1"));  break;
            case "2" :
                System.out.println(testService.test2("k2")); break;
            case "3" :
                System.out.println(testService.test3("k3")); break;
            case "4" :
                System.out.println(testService.test4("k4")); break;
            case "5" :
                Map<String, String> map = testService.test5(5);

                System.out.println(map); break;
            case "6" :
                System.out.println(testService.test6("k6")); break;
            case "7" :
                System.out.println(testService.test7("k7")); break;
            case "8":
                JSONObject object = new JSONObject();
                object.put("1","2");
                Object o = testService.test8(object);
                System.out.println(MyFastJsonUtil.toJSONString(o));
                break;
        }
        return new BaseResult<>(200,"成功","failure");
    }


}
