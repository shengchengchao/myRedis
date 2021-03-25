package com.xixi.myredis.tool.base;

import com.xixi.myredis.tool.util.RedisSerialUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author shengchengchao
 * @Description 操作redis的基本方法
 * @createTime 2021/3/24
 */
public abstract class RedisBaseOperate<T> {

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 判断key 是否在redis中
     * @param key
     * @return
     */
    Boolean hasKey(String key) {
        return (Boolean) redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) {
                byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
                return connection.exists(byteKey);
            }
        }, true);
    }

    /**
     * 设置过期时间
     * @param key
     * @param seconds
     */
    void expire(String key, long seconds) {
        redisTemplate.execute((RedisCallback) conn -> conn.expire(RedisSerialUtils.serial(key), seconds));
    }

    /**
     * 字符串格式的set 带有过期时间
     *
     * @param key    键
     * @param expire 过期时间
     * @param obj    结果
     */
    void redisSetWithExpire(String key, Long expire, Object obj) {

    }

    /**
     * 字符串格式的set 不带有过期时间
     *
     * @param key
     * @param field
     * @param value
     */
    void redisSet(String key, String field, String value) {

    }

    /**
     * 得到结果
     *
     * @param key         查询的key
     * @param targetClass 结果的类型
     * @return
     */
    T getKey(String key, Class targetClass) {
        return null;
    }

    /**
     * 删除 redis中的key
     *
     * @param key
     * @return
     */
    void delKey(String key) {
        redisTemplate.delete(RedisSerialUtils.serial(key));
    }

    /**
     * 将集合放入缓存中
     * @param key
     * @param expire
     * @param list
     */
    void setListToRedis(String key, Long expire, List<T> list) { }

    /**
     * 取出集合
     * @param key
     * @param targetClass
     * @return
     */
    List<T> getListFromKey(String key, Class<T> targetClass) {
        return null;
    }

    /**
     * 从哈希表中 得到数据
     *
     * @param key
     * @param field
     * @param targetClass
     * @return
     */
    T getFromHash(String key, String field, Class<Object> targetClass) {
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
    List<T> getListFromHash(String key, String field, Class<T> targetClass) {
        return null;
    }

    /**
     * 从哈希表中 得到键的所有值
     *
     * @param key
     * @return
     */
    Map<byte[], byte[]> getAllFromHash(String key) {
        return null;
    }


    /**
     * 将数据 加入到hash中
     *
     * @param key
     * @param fields 域
     * @param values 值
     */
    void setToHash(String key, String[] fields, String[] values) {

    }

    /**
     * 将数据 加入到hash中
     *
     * @param key
     * @param hashes 域-值
     */
    void setToHash(String key, Map<byte[], byte[]> hashes) {

    }

    /**
     * 将数据 加入到hash中 还有过期时间
     *
     * @param key
     * @param expire
     * @param field
     * @param obj
     */
    void setHashWithExpire(String key, Long expire, Object field, Object obj) {

    }

    /**
     * 将集合 加入到哈希表中
     * @param key
     * @param expire
     * @param field
     * @param list
     */
    void setListToRedisMap(String key, Long expire, String field, List<T> list) {

    }


    /**
     * 将集合放入缓存中 用集合的形式
     * @param key
     * @param expire
     * @param objects
     */
    void setListToRedisSet(String key, Long expire, List<T> objects) {

    }

    /**
     *
     * @param key
     * @param expire
     * @param value
     */
    void setToRedisSet(String key, Long expire, String value) {

    }

    /**
     * 向集合中添加元素
     * @param key
     * @param value
     * @return
     */
    void sAdd(String key, String value) {
    }

    /**
     * 判断一个元素是否属于集合
     * @param key
     * @param value
     * @return
     */
    Boolean sIsMember(String key, String value) {
        return null;
    }

    /**
     *
     * @param key
     * @param targetClass
     * @return
     */
    List<T> getListFromRedisSet(String key, Class<T> targetClass) {
        return null;
    }

    /**
     * 删除集合中 的数据
     * @param key
     * @param objects
     * @return
     */
    Long delListFromRedisSet(String key, List<T> objects) {
        return null;
    }

    /**
     * 集合中删除key
     * @param key
     * @param value
     * @return
     */
    Long delFromRedisSet(String key, String value) {
        return null;
    }

    /**
     * 获取多个key无序集合中的元素（去重），count表示个数
     * @param key
     * @param count
     * @return
     */
    Set<String> distinctRandomMembers(String key, Integer count) {
        return null;
    }

    /**
     * 在集合中加入数据
     * @param key
     * @param values
     * @return
     */
    Long sadd(String key, String... values) {
        return null;
    }

    /**
     * 返回有序集 key 中成员 member 的排名 逆序
     * @param key
     * @param member
     * @return
     */
    Long reverseRank(String key, String member) {
        return null;
    }

    /**
     * 为有序集 key 的成员 member 的 score 值加上增量 increment
     * @param key
     * @param member
     * @param score
     * @return
     */
    Double zIncrByKey(String key, Long member, Double score) {
        return null;
    }

    /**
     * 得到有序集合中 某个成员的分数
     * @param key
     * @param member
     * @return
     */
    Double zscore(String key, String member) {
        return null;
    }

    /**
     * 在有序集合中加入数据
     * @param key
     * @param member value
     * @param score 分数
     * @return
     */
    Boolean zadd(String key, String member, double score) {
        return null;
    }

    /**
     * 指定区间内，带有 score 值(可选)的有序集成员的列表 逆序的
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<ZSetOperations.TypedTuple<String>> zRevRangeWithScores(String key, long start, long end) {
        return null;
    }

    /**
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    List<ZSetOperations.TypedTuple<String>> zrangeWithScores(String key, long start, long end) {
        return null;
    }

    /**
     * 移除 有序集合中 从start到end的数据 闭区间
     * @param key
     * @param start
     * @param end
     * @return
     */
    Long zRemRangeByRank(String key, long start, long end) {
        return null;
    }

    /**
     * 将id 加入到有序集合中，id 同时作为 score 与结果
     *
     * @param key
     * @param id
     */
    void zAddValue(String key, Long id) {

    }


    /**
     * 将集合 加入到有序集合中
     *
     * @param key
     * @param endTagFlag 为false的话 要加入一个结束标志
     * @param ids
     */
    void redisZadd(String key, boolean endTagFlag, List<Long> ids) {

    }

    /**
     * 如果key 存在的话 就加入有序集合中
     * 采用lua脚本实现 保证原子性
     * @param key
     * @param id
     */
    void redisZaddIfExist(String key, long id,Long score) {

    }

    /**
     * 返回有序集 key 中， score 值介于 max 和 min 之间 按照score 逆序
     * @param key
     * @param min
     * @param max
     * @param offset
     * @param count
     * @return
     */
    List<Integer> redisZRevRangeByScore(String key, double min, double max, long offset, long count) {
        return null;
    }

}
