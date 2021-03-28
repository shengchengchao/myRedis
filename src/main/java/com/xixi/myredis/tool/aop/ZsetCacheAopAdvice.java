package com.xixi.myredis.tool.aop;

import com.google.common.collect.Lists;
import com.xixi.myredis.tool.annotation.RedisPageCount;
import com.xixi.myredis.tool.annotation.RedisParamKey;
import com.xixi.myredis.tool.annotation.RedisParamSize;
import com.xixi.myredis.tool.annotation.ZsetListCache;
import com.xixi.myredis.tool.series.MyRedisSerializer;
import com.xixi.myredis.tool.service.RedisZsetService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/26
 */
@Slf4j
@Component
@Aspect
public class ZsetCacheAopAdvice  extends RedisZsetService {

    public static final String BLAKE = ":";

    @Around("@annotation(com.xixi.myredis.tool.annotation.ZsetListCache)")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        //先开始校验必要的注解是否存在
        Map<String, Object> paramMap = getParamMap(joinPoint);
        if(!(paramMap.containsKey(RedisParamKey.class.getSimpleName()) && paramMap.containsKey(RedisPageCount.class.getSimpleName()))){
            log.info("缺少必要的注解 RedisParamKey RedisPageCount ");
            return joinPoint.proceed();
        }
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        ZsetListCache annotation = method.getAnnotation(ZsetListCache.class);
        if(annotation ==null ){
            return joinPoint.proceed();
        }
        int expire = annotation.expire();
        String redisKey = annotation.redisKey();
        //确定返回类型
        Type genericReturnType = method.getGenericReturnType();
        boolean assignableFrom = genericReturnType.getClass().isAssignableFrom(Class.class);
        MyRedisSerializer myRedisSerializer = null;
        if(!assignableFrom){
            ResolvableType resolvableType = ResolvableType.forMethodReturnType(method);
            //可能为List 或者 map
            if(resolvableType.resolve().isAssignableFrom(List.class)){
                Class<?> aClass = resolvableType.resolveGeneric(0,0);
                myRedisSerializer = new MyRedisSerializer(aClass);
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
        if(paramMap.containsKey(RedisParamKey.class.getSimpleName())){
            sb.append(paramMap.get(RedisParamKey.class.getSimpleName()));
        }
        log.info("查询的key为 {}",sb.toString());
        try {
            return read(joinPoint,sb.toString(),expire,myRedisSerializer,paramMap);
        }catch (Exception e){
            log.error("出现问题",e);
            return joinPoint.proceed();
        }
    }

    private Map<String, Object> getParamMap(ProceedingJoinPoint joinPoint) {
        Map<String, Object> map = new HashMap<>();
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i =0;i<parameterAnnotations.length;i++){
            for (int j=0;j<parameterAnnotations[i].length;j++){

                if(parameterAnnotations[i][j] instanceof RedisParamKey){
                    map.put(RedisParamKey.class.getSimpleName(), joinPoint.getArgs()[i]);
                }else if(parameterAnnotations[i][j] instanceof RedisPageCount){
                    map.put(RedisPageCount.class.getSimpleName(), joinPoint.getArgs()[i]);
                }else if(parameterAnnotations[i][j] instanceof RedisParamSize){
                    map.put(RedisParamSize.class.getSimpleName(), joinPoint.getArgs()[i]);

                }
            }

        }
        return map;
    }
}
