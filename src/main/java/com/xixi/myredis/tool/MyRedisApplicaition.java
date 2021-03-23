package com.xixi.myredis.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/16
 */
@SpringBootApplication
@ServletComponentScan
public class MyRedisApplicaition {

    public static void main(String[] args) {
        SpringApplication.run(MyRedisApplicaition.class, args);
    }

}
