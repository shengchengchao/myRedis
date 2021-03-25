package com.xixi.myredis.tool.aop;

import com.google.common.collect.Lists;
import com.xixi.myredis.tool.Constants.CommonConstants;
import com.xixi.myredis.tool.annotation.RedisParamKey;
import com.xixi.myredis.tool.annotation.RedisSimpleCache;
import com.xixi.myredis.tool.series.MyRedisSerializer;
import com.xixi.myredis.tool.service.RedisBaseService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author shengchengchao
 * @Descripn
 * @createTime 2021/3/23
 */
@Aspect
@Slf4j
@Component
public class SimpleCacheAopAdvice extends RedisBaseService {

    public static final String BLAKE = ":";


    @Pointcut(value = "@annotation(com.xixi.myredis.tool.annotation.RedisSimpleCache)")
    public void test(){

    }

    /**
     * 需要注意的问题 为了防止缓存雪崩，在设置过期时间的时候，要添加一个随机时间
     * 为了防止 两个请求参数相同，都没有在redis中找到数据，同时去执行查询，
     * 在查询之后，需要再次判断是否存在缓存 存在就不将数据加入缓存中
     * 后续需要考虑为了防止缓存穿透的问题 要进行一个setnx的 互斥锁
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around(value = "test()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        //1. 先去校验注解 RedisKey 是否存在 是否只有一个


        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        log.info("执行方法：{}",method.getName());
        List paramKey = getParamKey(method, joinPoint);
        if(paramKey.size()>1){
            log.error("{} 不支持多个RedisParamKey注解，将直接执行方法",method);
            return joinPoint.proceed();
        }
        RedisSimpleCache annotation = method.getAnnotation(RedisSimpleCache.class);
        int expire = annotation.expire();
        String redisKey = annotation.redisKey();
        boolean reCalFlag = annotation.reCalFlag();
        //2.得到返回类型 来确定 序列化的规则
        Type genericReturnType = method.getGenericReturnType();
        boolean assignableFrom = genericReturnType.getClass().isAssignableFrom(Class.class);
        MyRedisSerializer myRedisSerializer = null;
        if(!assignableFrom){
            ResolvableType resolvableType = ResolvableType.forMethodReturnType(method);
            //可能为List 或者 map
            if(genericReturnType.getClass().isAssignableFrom(List.class)){
                Class<?> aClass = resolvableType.resolveGeneric(0);
                myRedisSerializer = new MyRedisSerializer(List.class);
                myRedisSerializer.setClassList(Lists.newArrayList(aClass));
            }else if(genericReturnType.getClass().isAssignableFrom(Map.class)){
                myRedisSerializer = new MyRedisSerializer(Map.class, CommonConstants.MAP);
                Class<?> keyClass = resolvableType.resolveGeneric(0);
                Class<?> valueClass = resolvableType.resolveGeneric(0);
                myRedisSerializer.setClassList(Lists.newArrayList(keyClass,valueClass));
            }else {
                myRedisSerializer = new MyRedisSerializer(resolvableType.resolve());

            }
        }else {
            String typeName = genericReturnType.getTypeName();
            myRedisSerializer = new MyRedisSerializer(Class.forName(typeName));
        }
        //3. 组装去redis 查询的key
        StringBuffer sb = new StringBuffer();
        sb.append(redisKey).append(BLAKE);
        if(!paramKey.isEmpty() && paramKey.get(0)!=null){
            sb.append(paramKey.get(0));
        }
        log.info("查询的key为 {}",sb.toString());

        try {
            return read(joinPoint,sb.toString(),expire,reCalFlag,myRedisSerializer);
        } catch (Exception e) {
            log.error("查询 key：{} ,出错了",sb.toString(),e.getMessage());
            return joinPoint.proceed();
        }

    }




    /**
     * 得到带有RedisParamKey 注解的参数
     * @param method
     * @param joinPoint
     * @return
     */
    private List getParamKey(Method method,ProceedingJoinPoint joinPoint) {
        List list = new ArrayList();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i=0; i<parameterAnnotations.length;i++){
            for (int j =0;j< parameterAnnotations[i].length;j++){
                if(parameterAnnotations[i][j] instanceof RedisParamKey){
                    Object[] args = joinPoint.getArgs();
                    list.add(args[i]);
                }
            }
        }
        return list;
    }



}
