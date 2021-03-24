package com.xixi.myredis.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/16
 */
@SpringBootApplication
@ServletComponentScan
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class MyRedisApplicaition {

    public static void main(String[] args) {
        SpringApplication.run(MyRedisApplicaition.class, args);
    }

}
