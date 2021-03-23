package com.xixi.myredis.tool.rest;

import com.xixi.myredis.tool.base.BaseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.JedisPoolConfig;

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
    private JedisPoolConfig jedisPoolConfig;

    @ApiOperation(value = "测试redis")
    @GetMapping("/redis")
    @ResponseBody
    public BaseResult testData(@RequestParam String body) {
        int maxIdle = jedisPoolConfig.getMaxIdle();
        redisTemplate.opsForValue().set("k1112",body);
        if(redisTemplate.hasKey("k1112")){
            return new BaseResult<>(200,"成功",redisTemplate.opsForValue().get("k1112"));
        }
        return new BaseResult<>(200,"成功","failure");
    }
}
