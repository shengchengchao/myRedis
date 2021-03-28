package com.xixi.myredis.tool.service;

import com.xixi.myredis.tool.annotation.RedisPageCount;
import com.xixi.myredis.tool.annotation.RedisParamKey;
import com.xixi.myredis.tool.annotation.RedisParamSize;
import com.xixi.myredis.tool.base.TupleObject;
import com.xixi.myredis.tool.base.ZsetOperate;
import com.xixi.myredis.tool.series.MyRedisSerializer;
import com.xixi.myredis.tool.util.RedisSerialUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/26
 */
@Slf4j
public class RedisZsetService<T> extends ZsetOperate {

    public static final int REDIS_LENGTH =2000;
    public static final String TYPE_INIT = "init";
    public static final String TYPE_READ = "read";

    public static final byte[] emptyByte = new byte[]{0};
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 去redis中查询数据，没有数据的话，将结果查询出来，再放入到redis中。
     * @description:
     * @author: shengchengchao
     * @date 2021/3/24
     * @return
     */
    public List<TupleObject<T>> read(ProceedingJoinPoint joinPoint, String key, int expire, MyRedisSerializer myRedisSerializer, Map<String, Object> map) throws Throwable {
        //先去redis中查询结果 没法查出来的话 就去
        List<TupleObject<T>> result = new ArrayList<>();
        Object pageCount =  map.get(RedisPageCount.class.getSimpleName());
        Object pageSize =  map.get(RedisParamSize.class.getSimpleName());
        int count=0;
        int size = 2000;
        try {
            count =  Integer.parseInt(pageCount.toString());
            size  =  Integer.parseInt(pageSize.toString());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            log.error(" pageCount ，pageSize  无法转换成数字类型，使用默认值");
        }
        Object[] oldArgs = joinPoint.getArgs();
        //存在缓存 去查询缓存
        if(hasKey(key)){
            List<RedisZSetCommands.Tuple> resultFromRedis = getResultFromRedis(key, count, size);

            //需要判断是否需要重新去数据库获取
            int cacheSize = 0;
            //如果resultFromRedis为null，表明数据库中还有redis中未缓存的数据
            if(!CollectionUtils.isEmpty(resultFromRedis)) {
                cacheSize = resultFromRedis.size();
            }

            RedisZSetCommands.Tuple last = null;
            if(cacheSize > 0){
                last = resultFromRedis.get(resultFromRedis.size() - 1);
            }

            if(last != null && last.getValue().length == 1 && last.getValue()[0] == 0){
                //最后一条有值并且最后一条等于-1，表明该数据条目前就只有这些，数据库里也没有了
                //把最后一条删除后返回即可
                resultFromRedis.remove(resultFromRedis.size() - 1);

                for(RedisZSetCommands.Tuple each:resultFromRedis){
                    result.add(new TupleObject(myRedisSerializer.deserialize(each.getValue()),each.getScore().longValue()));
                }
            } else {
                if (cacheSize < size) {
                    //如果没取够数量，说明需要去数据库中构建
                    log.debug("SecretPageListCache 没取够数量，去数据库中构建");
                    List<TupleObject<T>> dbList = getResult(joinPoint,count,size,TYPE_READ);
                    redisZadd(key,dbList,myRedisSerializer,size,expire);
                    result = dbList;
                }else{
                    log.debug("SecretPageListCache 缓存获取，返回");
                    for(RedisZSetCommands.Tuple each:resultFromRedis){
                        result.add(new TupleObject(myRedisSerializer.deserialize(each.getValue()),each.getScore().longValue()));
                    }
                }
            }
            return result;
        }else {
            //去查询数据，再存入缓存
            List<TupleObject<T>> resultDB = getResult(joinPoint, count, size, TYPE_INIT);
            if(CollectionUtils.isEmpty(resultDB)){
                return null;
            }
            //加入到redis中
            redisZadd(key,resultDB,myRedisSerializer,size,expire);
            //注意 这里没有在redis中取得数据 要使用原先的数据
            return (List<TupleObject<T>>)joinPoint.proceed(oldArgs);
        }
    }

    /**
     * 从redis 中得到数据
     * @param key
     * @param count 小值
     * @param size  最大值
     */
    private List<RedisZSetCommands.Tuple> getResultFromRedis(String key, int count, int size) {
        RedisZSetCommands.Range range = new RedisZSetCommands.Range();

        RedisZSetCommands.Limit limit = new RedisZSetCommands.Limit();
        limit.count(size);
        limit.offset((count-1)*size);
       return (List<RedisZSetCommands.Tuple>)redisTemplate.execute((RedisCallback) con->{
           byte[] serial = RedisSerialUtils.serial(key);
           Set<RedisZSetCommands.Tuple> resultByte = new HashSet<>();

           resultByte = con.zRangeByScoreWithScores(serial, range, limit);

           if(CollectionUtils.isEmpty(resultByte)){
               return null;
           }
           List<RedisZSetCommands.Tuple> result = new ArrayList();
           for(RedisZSetCommands.Tuple each:resultByte){
               result.add(each);
           }

           return result;
        });
    }


    /**
     * 得到数据
     * @param joinPoint
     * @param count
     * @param size
     * @param type
     * @return
     * @throws Throwable
     */
    private List<TupleObject<T>> getResult(ProceedingJoinPoint joinPoint, int count, int size,String type) throws Throwable {

        Boolean flag = true;
        if(TYPE_INIT.equals(type)){
            flag = false;
        }
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = new Object[parameterAnnotations.length];
        for (int i =0;i<parameterAnnotations.length;i++){
            for (int j=0;j<parameterAnnotations[i].length;j++){
                if(parameterAnnotations[i][j] instanceof RedisParamKey){
                     args[i] = joinPoint.getArgs()[i];
                }else if(parameterAnnotations[i][j] instanceof RedisPageCount){
                    if(flag){
                        args[i] = joinPoint.getArgs()[i];
                    }else {

                        args[i] = (long)count;
                    }
                }else if(parameterAnnotations[i][j] instanceof RedisParamSize){
                    if(flag){
                        args[i] = joinPoint.getArgs()[i];
                    }else {
                        args[i] = (long)size;
                    }
                }
            }
        }

        try {
            return (List<TupleObject<T>>)joinPoint.proceed(args);
        } catch (Throwable throwable) {
            log.error("出现问题",throwable);
            return null;
        }
    }

    /**
     *  添加数据
     * @param key 键
     * @param resultDB 得到的数据
     * @param myRedisSerializer
     * @param size 要求的数据大小
     * @param expire 过期时间
     */
    private void redisZadd(String key,List<TupleObject<T>> resultDB ,MyRedisSerializer myRedisSerializer,int size,int expire) {
        byte[] serial = RedisSerialUtils.serial(key);
        redisTemplate.execute((RedisCallback) con->{
            for (TupleObject tupleObject:resultDB){
                con.zAdd(serial,tupleObject.getScore(),myRedisSerializer.serialize(tupleObject.getMember()));
            }
            if (resultDB.size() < size) {
                //如果取到的数据小于希望得到的数据量，说明数据库中已经没有数据了
                //增加空标识，表明数据库中也没有数据了
                con.zAdd(serial, 0, emptyByte);
            }
            if(expire!=-1){
                con.expire(serial,expire);
            }
            return null;
        });
    }
}

