package com.xixi.myredis.tool.base;

import com.xixi.myredis.tool.Constants.CommonConstants;
import com.xixi.myredis.tool.series.MyRedisSerializer;
import com.xixi.myredis.tool.util.MyFastJsonUtil;
import com.xixi.myredis.tool.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/23
 */
@Slf4j
public class RedisBase  {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 去redis中查询数据，没有数据的话，将结果查询出来，再放入到redis中。
     * @description:
     * @author: shengchengchao
     * @date 2021/3/24
     * @return
     */
    public Object read(ProceedingJoinPoint joinPoint, String key, int expire, boolean reCalFlag, MyRedisSerializer myRedisSerializer) throws Throwable {
        //先去redis中去查询
        //使用Pipeline可以批量执行redis命令，防止多个命令建立多个连接
        byte[] serialKey = RedisUtils.serial(key);
        List redisResult = redisTemplate.executePipelined((RedisCallback) conn -> {
            byte[] bytes = conn.get(serialKey);
            return null;
        }, null);

        byte[] bytes = (byte[]) redisResult.get(0);
        //没有缓存
        if(bytes ==null ){
            log.info("没有找到缓存，执行方法");
            Object resultSave = getResultSave(joinPoint, serialKey, expire, reCalFlag, myRedisSerializer);
            log.info("已得到结果并存入redis中");
            return resultSave;
        }else if(bytes.length==1 && bytes[0] ==0 ){
            //缓存为null
            return null;
        }else {
            //有缓存 将数据反序列化
            try {
                Object result = deserializeResult(myRedisSerializer, bytes);
                if(reCalFlag){
                    redisTemplate.expire(serialKey,expire, TimeUnit.SECONDS);
                }
                return result;
            } catch (Exception e) {
                log.error("从redis中反序列化出错",e);
                return joinPoint.proceed();
            }
        }
    }

    /**
     *  执行数据 并将结果保存到redis中
     * @param joinPoint
     * @param key
     * @param expire
     * @param reCalFlag
     * @param myRedisSerializer
     * @return
     */
    private Object getResultSave(ProceedingJoinPoint joinPoint, byte[] serialKey, int expire, boolean reCalFlag, MyRedisSerializer myRedisSerializer) {
        try {
            Object proceed = joinPoint.proceed();
            if(proceed  !=null){
                //再次查找redis 确定结果
                List redisResult = redisTemplate.executePipelined((RedisCallback) conn -> {
                    byte[] bytes = conn.get(serialKey);
                    return null;
                }, null);
                byte[] bytes = (byte[]) redisResult.get(0);
                //找到结果的话 进行反序列化
                if(bytes !=null){
                    Object deserialize = deserializeResult(myRedisSerializer,bytes);
                    return deserialize;
                }else {
                    //将数据存储到redis中
                    saveRedis(proceed,myRedisSerializer,serialKey,expire);
                }
            }
            return proceed;
        } catch (Throwable throwable) {
            log.error("出现错误",throwable.getMessage());
        }
        return null;
    }

    /**
     * 保存数据到redis
     * @param result
     * @param myRedisSerializer
     */
    private void saveRedis(Object result, MyRedisSerializer myRedisSerializer,byte[] serialKey,int expire) {
        if(result!=null){
            //判断数据的类型 是否为null
            byte[] serialize = myRedisSerializer.serialize(result);
            redisTemplate.executePipelined((RedisCallback)con->{
                con.set(serialKey,serialize);
                if(expire!=CommonConstants.permanent){
                    con.expire(serialKey,expire);
                }
                return null;
            });

        }
    }

    /**
     * 反序列化 数据
     * @param myRedisSerializer 自定义的序列化
     * @param bytes 结果
     * @return
     */
    private Object deserializeResult(MyRedisSerializer myRedisSerializer,byte[] bytes){
        if(CommonConstants.MAP.equals(myRedisSerializer.getTypeName())){
            List<Class> classList = myRedisSerializer.getClassList();
            return MyFastJsonUtil.json2Map(new String(bytes), classList.get(0),classList.get(1));
        }else {
            return myRedisSerializer.deserialize(bytes);
        }

    }


}
