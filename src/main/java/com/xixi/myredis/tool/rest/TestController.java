package com.xixi.myredis.tool.rest;

import com.alibaba.fastjson.JSONObject;
import com.xixi.myredis.tool.annotation.RequestProtect;
import com.xixi.myredis.tool.base.BaseResult;
import com.xixi.myredis.tool.base.TupleObject;
import com.xixi.myredis.tool.service.TestService;
import com.xixi.myredis.tool.test.ListPageTest;
import com.xixi.myredis.tool.test.TestDRo;
import com.xixi.myredis.tool.util.MyFastJsonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    private ListPageTest listPageTest;



    @PostMapping("/test")
    @RequestProtect(expire = 100,body = "[\"name\",\"age\"]")
    @ResponseBody
    public void test(@RequestBody TestDRo testDRo){
        List<TupleObject<String>> list = listPageTest.list("zset",2L, 8L);
        System.out.println(list.toString());
    }


}
