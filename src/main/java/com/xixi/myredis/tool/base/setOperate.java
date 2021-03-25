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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/25
 */
@Slf4j
public class setOperate<T> extends RedisBaseOperate<T> {


    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 将集合放入缓存中 用集合的形式
     * 采用事务的方式 保证原子性
     * @param key
     * @param expire
     * @param objects
     */
    @Override
    void setListToRedisSet(String key, Long expire, List<T> objects) {
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
                redisConnection.multi();
                int i = 0;
                for (Object obj : objects) {
                    redisConnection.sAdd(byteKey, MyFastJsonUtil.toJSONString(obj).getBytes());
                    if (i++ == 1000) {
                        redisConnection.exec();
                        redisConnection.multi();
                    }
                }
                redisConnection.exec();
                redisConnection.expire(byteKey, expire);
                return Boolean.TRUE;
            }
        });
    }

    /**
     *
     * @param key
     * @param expire
     * @param value
     */
    @Override
    void setToRedisSet(String key, Long expire, String value) {
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
                redisConnection.sAdd(byteKey, MyFastJsonUtil.toJSONString(value).getBytes());
                redisConnection.expire(byteKey, expire);
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 向集合中添加元素
     * @param key
     * @param value
     * @return
     */
    @Override
    void sAdd(String key, String value) {
        byte[] serialKey = RedisSerialUtils.serial(key);
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.sAdd(serialKey, MyFastJsonUtil.toJSONString(value).getBytes());
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 判断一个元素是否属于集合
     * @param key
     * @param value
     * @return
     */
    @Override
    Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key,value);
    }

    /**
     *
     * @param key
     * @param targetClass
     * @return
     */
    @Override
    List<T> getListFromRedisSet(String key, Class<T> targetClass) {
        List<T> list = new ArrayList<>();
        byte[] serialKey = RedisSerialUtils.serial(key);
        return (List<T>) redisTemplate.execute(new RedisCallback() {
            @Override
            public List doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Set<byte[]> bytes = redisConnection.sMembers(serialKey);
                if(!CollectionUtils.isEmpty(bytes)){
                    Iterator<byte[]> iterator = bytes.iterator();
                    while (iterator.hasNext()){
                        byte[] next = iterator.next();
                        T t = MyFastJsonUtil.parseObject(new String(next), targetClass);
                        list.add(t);
                    }
                }
                return list;
            }
        });

    }

    /**
     * 删除集合中 的数据
     * @param key
     * @param objects
     * @return
     */
    @Override
    Long delListFromRedisSet(String key, List<T> objects) {
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                int i=0;
                int count=0;
                byte[] byteKey = RedisSerialUtils.serial(key);
                redisConnection.multi();
                for (T t :objects){
                    redisConnection.sRem(byteKey,RedisSerialUtils.serial(t));
                    if(i++ ==1000){
                        redisConnection.exec();
                        redisConnection.multi();
                    }
                    count++;
                }
                return count;
            }
        });
    }

    /**
     * 集合中删除key
     * @param key
     * @param value
     * @return
     */
    @Override
    Long delFromRedisSet(String key, String value) {
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = RedisSerialUtils.serial(key);
               return redisConnection.sRem(byteKey, MyFastJsonUtil.toJSONString(value).getBytes());
            }
        });
    }

    /**
     * 获取多个key无序集合中的元素（去重），count表示个数
     * @param key
     * @param count
     * @return
     */
    @Override
    Set<String> distinctRandomMembers(String key, Integer count) {
        Set<String> listData = redisTemplate.opsForSet().distinctRandomMembers(key, count);
        return listData;
    }

    /**
     * 在集合中加入数据
     * @param key
     * @param values
     * @return
     */
    @Override
    Long sadd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }
}
