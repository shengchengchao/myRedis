package com.xixi.myredis.tool.base;

import com.xixi.myredis.tool.util.MyFastJsonUtil;
import com.xixi.myredis.tool.util.RedisSerialUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/25
 */
@Slf4j
public abstract class StringOperate<T>  extends RedisBaseOperate<T>{


    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 字符串格式的set 带有过期时间
     *
     * @param key    键
     * @param expire 过期时间
     * @param obj    结果
     */
    @Override
    public void redisSetWithExpire(String key, Long expire, Object obj) {
        byte[] serialKey = RedisSerialUtils.serial(key);
        log.info("需要设置的参数 {}",serialKey);
        redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) {
                byte[] serialValue = RedisSerialUtils.serial(MyFastJsonUtil.toJSONString(obj));
                connection.set(serialKey,serialKey);
                connection.expire(serialKey,expire);
                return true;
            }
        });
    }

    /**
     * 字符串格式的set 不带有过期时间
     *
     * @param key
     * @param field
     * @param value
     */
    @Override
    public void redisSet(String key, String field, String value) {
        byte[] serialKey = RedisSerialUtils.serial(key);
        log.info("需要设置的参数 {}",serialKey);
        redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) {
                byte[] serialValue = RedisSerialUtils.serial(MyFastJsonUtil.toJSONString(value));
                connection.set(serialKey,serialKey);
                return true;
            }
        });
    }

    /**
     * 得到结果
     *
     * @param key         查询的key
     * @param targetClass 结果的类型
     * @return
     */
    @Override
    T getKey(String key, Class targetClass) {
        byte[] serialKey = RedisSerialUtils.serial(key);
        log.info("需要取出的参数 {}",serialKey);
        return (T) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] value = redisConnection.get(serialKey);
                if(value!=null){
                    Object obj = MyFastJsonUtil.parseObject(new String(value), targetClass);
                    return obj;
                }
                return null;
            }
        });
    }



    /**
     * 将集合放入缓存中
     * @param key
     * @param expire
     * @param list
     */
    @Override
    void setListToRedis(String key, Long expire, List<T> list) {
        if(CollectionUtils.isEmpty(list)){
            return ;
        }
        redisSetWithExpire(key,expire,list);
    }

    /**
     * 取出集合
     * @param key
     * @param targetClass
     * @return
     */
    @Override
    List<T> getListFromKey(String key, Class<T> targetClass) {
        byte[] serialKey = RedisSerialUtils.serial(key);
        log.info("需要取出的参数 {}",serialKey);
        return (List) redisTemplate.execute(new RedisCallback<List>() {
            @Override
            public List doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] value = redisConnection.get(serialKey);
                if(value!=null){
                    List<T> ts = MyFastJsonUtil.parseArrays(new String(value), targetClass);
                    return ts;
                }
                return new ArrayList<T>();
            }
        });
    }
}
