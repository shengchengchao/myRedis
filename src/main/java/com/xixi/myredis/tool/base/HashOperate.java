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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/25
 */
@Slf4j
public abstract class HashOperate<T> extends RedisBaseOperate<T> {

    @Autowired 
    private RedisTemplate redisTemplate;

    /**
     * 从哈希表中 得到数据
     *
     * @param key
     * @param field
     * @param targetClass
     * @return
     */
    @Override
    protected String getFromHash(String key, String field, Class targetClass) {

        Object o = redisTemplate.opsForHash().get(key, field);
        if(o!=null){
            return o.toString();

        }
        return null;
    }

    /**
     * 从哈希表中 得到集合数据
     *
     * @param key
     * @param field
     * @param targetClass
     * @return
     */

    @Override
    protected List<T> getListFromHash(String key, String field, Class targetClass) {

        return (List) redisTemplate.execute(new RedisCallback<List>() {
            byte[] serialKey = RedisSerialUtils.serial(key);
            byte[] serialField = RedisSerialUtils.serial(field);
            @Override
            public List doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] value = redisConnection.hGet(serialKey,serialField);
                if(value!=null){
                    List<T> ts = MyFastJsonUtil.parseArrays(new String(value), targetClass);
                    return ts;
                }
                return new ArrayList<T>();
            }
        });
    }

    /**
     * 从哈希表中 得到键的所有值
     *
     * @param key
     * @return
     */
    @Override
    protected  Map<byte[], byte[]> getAllFromHash(String key) {
        return (Map<byte[], byte[]>) redisTemplate.execute(new RedisCallback<Map>() {
            byte[] serialKey = RedisSerialUtils.serial(key);

            @Override
            public Map doInRedis(RedisConnection redisConnection) throws DataAccessException {
                Map<byte[], byte[]> map = redisConnection.hGetAll(serialKey);

                return map;
            }
        });
    }


    /**
     * 将数据 加入到hash中
     *
     * @param key
     * @param fields 域
     * @param values 值
     */
    @Override
    protected void setToHash(String key, String[] fields, String[] values) {
        Map<byte[], byte[]> hashMap = new HashMap<>();
        for (int i = 0;i<fields.length;i++){
            hashMap.put(RedisSerialUtils.serial(fields[i]),RedisSerialUtils.serial(values[i]));
        }
        setToHash(key,hashMap);
    }

    /**
     * 将数据 加入到hash中
     *
     * @param key
     * @param hashes 域-值
     */
    @Override
    protected void setToHash(String key, Map<byte[], byte[]> hashes) {
        redisTemplate.execute(new RedisCallback() {
            byte[] serialKey = RedisSerialUtils.serial(key);
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.hMSet(serialKey,hashes);
                return true;
            }
        });
    }

    /**
     * 将数据 加入到hash中 还有过期时间
     *
     * @param key
     * @param expire
     * @param field
     * @param obj
     */
    @Override
    protected  void setHashWithExpire(String key, Long expire, Object field, Object obj) {
        redisTemplate.execute(new RedisCallback() {
            byte[] serialKey = RedisSerialUtils.serial(key);
            byte[] serialField = RedisSerialUtils.serial(field);
            byte[] serialObj =  RedisSerialUtils.serial(obj);
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.hSet(serialKey,serialField,serialObj);
                redisConnection.expire(serialKey,expire);
                return true;
            }
        });
    }

    /**
     * 将集合 加入到哈希表中
     * @param key
     * @param expire
     * @param field
     * @param list
     */
    @Override
    protected  void setListToRedisMap(String key, Long expire, String field, List<T> list) {
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        setHashWithExpire(key,expire,field,list);
    }
}
