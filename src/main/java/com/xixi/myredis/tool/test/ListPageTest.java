package com.xixi.myredis.tool.test;

import com.xixi.myredis.tool.annotation.RedisPageCount;
import com.xixi.myredis.tool.annotation.RedisParamKey;
import com.xixi.myredis.tool.annotation.RedisParamSize;
import com.xixi.myredis.tool.annotation.ZsetListCache;
import com.xixi.myredis.tool.base.TupleObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/27
 */
@Component
public class ListPageTest {

    private List<TupleObject<String>> list = new ArrayList<>();

    @PostConstruct
    private void test(){
        for (int i=0;i<17;i++){
            TupleObject<String> stringTupleObject = new TupleObject<String>(i + "1", (long)i);
            list.add(stringTupleObject);
        }

    }
    @ZsetListCache(redisKey = "TEST")
    public List<TupleObject<String>> list (@RedisParamKey String key, @RedisPageCount Long count, @RedisParamSize Long size){
        System.out.println("直接执行");
        List<TupleObject<String>> collect = list.stream().skip((count - 1) * size).limit(size).
                collect(Collectors.toList());
        return collect;
    }
}
