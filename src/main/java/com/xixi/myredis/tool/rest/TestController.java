package com.xixi.myredis.tool.rest;

import com.alibaba.fastjson.JSONObject;
import com.xixi.myredis.tool.base.BaseResult;
import com.xixi.myredis.tool.service.TestService;
import com.xixi.myredis.tool.util.MyFastJsonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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








}
