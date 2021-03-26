package com.xixi.myredis.tool.base;

import com.xixi.myredis.tool.util.RedisSerialUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DefaultTuple;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/25
 */
public abstract class ZsetOperate<T> extends RedisBaseOperate<T> {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    /**
     * 返回有序集 key 中成员 member 的排名 逆序
     * @param key
     * @param member
     * @return
     */
    @Override
    protected  Long reverseRank(String key, String member) {

        return redisTemplate.opsForZSet().reverseRank(key,member);
    }

    /**
     * 为有序集 key 的成员 member 的 score 值加上增量 increment
     * @param key
     * @param member
     * @param score
     * @return
     */
    protected Double zIncrByKey(String key, String member, Double score) {
        return redisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    /**
     * 得到有序集合中 某个成员的分数
     * @param key
     * @param member
     * @return
     */
    @Override
    protected Double zscore(String key, String member) {
        return redisTemplate.opsForZSet().score(key,member);
    }

    /**
     * 在有序集合中加入数据
     * @param key
     * @param member value
     * @param score 分数
     * @return
     */
    @Override
    protected Boolean zadd(String key, String member, double score) {
        return redisTemplate.opsForZSet().add(key, member, score);
    }

    /**
     * 指定区间内，带有 score 值(可选)的有序集成员的列表 逆序的
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    protected  List<ZSetOperations.TypedTuple<String>> zRevRangeWithScores(String key, long start, long end) {

        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().reverseRangeByScore(key, start, end);
        return new ArrayList<>(set);
    }

    /**
     *
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    protected  List<ZSetOperations.TypedTuple<String>> zrangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<String>> set = redisTemplate.opsForZSet().rangeWithScores(key, start, end);
        return new ArrayList<>(set);
    }

    /**
     * 移除 有序集合中 从start到end的数据 闭区间
     * @param key
     * @param start
     * @param end
     * @return
     */
    @Override
    protected   Long zRemRangeByRank(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, start, end);
    }

    /**
     * 将id 加入到有序集合中，id 同时作为 score 与结果
     *
     * @param key
     * @param id
     */
    protected void zAddValue(String key, String id) {
        redisTemplate.opsForZSet().add(key, id, Double.valueOf(id));
    }


    /**
     * 将集合 加入到有序集合中
     *
     * @param key
     * @param endTagFlag 为false的话 要加入一个结束标志
     * @param ids
     */
    @Override
    protected void redisZadd(String key, boolean endTagFlag, List<Long> ids) {
        redisTemplate.execute(new RedisCallback() {
            @Override
            public Boolean doInRedis(RedisConnection redisConnection) throws DataAccessException {
                byte[] byteKey = redisTemplate.getStringSerializer().serialize(key);
                Set<RedisZSetCommands.Tuple> tupleSet = new HashSet<>();
                for (long id : ids) {
                    tupleSet.add(new DefaultTuple(RedisSerialUtils.serial(id), (double) id));
                }
                //加上-1结尾标志
                if (endTagFlag) {
                    tupleSet.add(new DefaultTuple(RedisSerialUtils.serial(-1), (double) -1));
                }
                redisConnection.zAdd(byteKey, tupleSet);
                return Boolean.TRUE;
            }
        });
    }

    /**
     * 如果key 存在的话 就加入有序集合中
     * 采用lua脚本实现 保证原子性
     * @param key
     * @param id
     */
    @Override
    protected void redisZaddIfExist(String key, long id,Long score) {
        byte[] valueByte = RedisSerialUtils.serial(score);
        byte[] scoreByte = RedisSerialUtils.serial(score);
        byte[] keyByte = RedisSerialUtils.serial(key);
        redisTemplate.execute((RedisCallback) conn->{
            conn.eval(RedisSerialUtils.serial("local exist=redis.call('exists',KEYS[1]); if exist==1 then redis.call('zadd',KEYS[1],ARGV[1],ARGV[2]) end"),
                    ReturnType.VALUE,1,keyByte,scoreByte,valueByte);
            return null;
        });
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
    @Override
    protected List<Integer> redisZRevRangeByScore(String key, double min, double max, long offset, long count) {
        Set<Integer> set = redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, offset, count);
        return new ArrayList<>(set);
    }

}
