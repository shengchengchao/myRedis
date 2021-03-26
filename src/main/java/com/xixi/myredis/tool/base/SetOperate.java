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
public class SetOperate<T> extends RedisBaseOperate<T> {


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
    protected void setListToRedisSet(String key, Long expire, List<T> objects) {
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = RedisSerialUtils.serial(key);
                redisConnection.multi();
                int i = 0;
                for (Object obj : objects) {
                    redisConnection.sAdd(byteKey, RedisSerialUtils.serial(obj));
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
    protected void setToRedisSet(String key, Long expire, String value) {
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = RedisSerialUtils.serial(key);
                redisConnection.sAdd(byteKey, MyFastJsonUtil.toJSONString(value).getBytes());
                redisConnection.expire(byteKey, expire);
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 在集合中加入数据
     * @param key
     * @param values
     * @return
     */
    @Override
    protected Long sadd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }
    /**
     * 向集合中添加元素
     * @param key
     * @param value
     * @return
     */
    @Override
    protected  void sAdd(String key, String value) {
        byte[] serialKey = RedisSerialUtils.serial(key);
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.sAdd(serialKey,RedisSerialUtils.serial(value));
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
    protected  Boolean sIsMember(String key, String value) {
        return redisTemplate.opsForSet().isMember(key,value);
    }

    /**
     *
     * @param key
     * @param targetClass
     * @return
     */
    @Override
    protected  List<T> getListFromRedisSet(String key, Class<T> targetClass) {
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
    protected Long delListFromRedisSet(String key, List<T> objects) {
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Long i=0L;
                Long count=0L;
                byte[] byteKey = RedisSerialUtils.serial(key);
                redisConnection.multi();
                for (Object t :objects){
                    Long aLong = redisConnection.sRem(byteKey, RedisSerialUtils.serial(t));
                    if(i++ ==1000){
                        redisConnection.exec();
                        redisConnection.multi();
                    }
                    count+=1;
                }
                redisConnection.exec();
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
    protected  Long delFromRedisSet(String key, String value) {
        return (Long) redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = RedisSerialUtils.serial(key);
               return redisConnection.sRem(byteKey, RedisSerialUtils.serial(value));
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
    protected Set<String> distinctRandomMembers(String key, Integer count) {
        Set<String> listData = redisTemplate.opsForSet().distinctRandomMembers(key, count);
        return listData;
    }


}
