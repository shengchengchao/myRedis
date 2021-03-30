package com.xixi.myredis.tool.aop;

import com.xixi.myredis.tool.annotation.RequestProtect;
import com.xixi.myredis.tool.util.MD5Util;
import com.xixi.myredis.tool.util.MyFastJsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/29
 */
@Aspect
@Slf4j
@Component
public class RequestProtectAopAdvice {
    @Autowired
    private RedisTemplate redisTemplate;

    public static final String DEFAULT_SPILT = "_";
    public static final String PROTECT = "PROTECT";
    public static final String INCREASE = "INCREASE";

    @Around(value = "@annotation(com.xixi.myredis.tool.annotation.RequestProtect)")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        //得到key
        log.info("进入");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequestProtect annotation = method.getAnnotation(RequestProtect.class);
        String key = getKey(annotation,joinPoint);
        //进行加密，避免参数过长
        String changeKey = MD5Util.md532(key);
        Object obj = redisTemplate.opsForValue().get(PROTECT+changeKey);
        if(obj==null){
            //判断是否要开启保护 防止缓存击穿
            redisTemplate.opsForValue().set(PROTECT+changeKey,"");
            redisTemplate.expire(PROTECT+changeKey, 1, TimeUnit.SECONDS);
            //保证原子性
            Long incr = redisTemplate.opsForValue().increment(INCREASE + changeKey, 1);
            if(incr ==1L){
                try {
                    redisTemplate.expire(PROTECT + changeKey, annotation.expire(), TimeUnit.SECONDS);
                    Object proceed = joinPoint.proceed();
                    redisTemplate.delete(INCREASE+changeKey);
                    return proceed;
                } catch (Exception e) {
                    log.error("出错了,{}",e);
                }
            }else {
                log.error("请求频率过高，请等待");
            }
        }else {
            Long expire = redisTemplate.getExpire(PROTECT+changeKey);
            log.error("请等待{}秒,后继续请求",expire);
        }
        return null;
    }

    /**
     * 拼接得到缓存的key  url+method+(token)+(body)
     * @param annotation
     * @param joinPoint
     * @return
     */
    private String getKey(RequestProtect annotation,ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        StringBuilder sb = new StringBuilder();
        String requestURI = request.getRequestURI();

        sb.append(requestURI).append(DEFAULT_SPILT);
        sb.append(signature.getMethod().getName()).append(DEFAULT_SPILT);
        String token = annotation.tokenField();
        if(StringUtils.isNotBlank(token)){
            String header = request.getHeader(token);
            sb.append(header).append(DEFAULT_SPILT);
        }
        //得到参数的内容
        if(StringUtils.isNotBlank(annotation.body())){
            Object[] args = joinPoint.getArgs();
            String param = getRequestBody(args, annotation.body(), sb);
            return param;
        }
        return sb.toString();
    }

    /**
     * 得到请求体中 需要的参数，最好是将参数放在一个对象中，如果不是一个对象，对于其中字段名相同的不同对象，默认只取第一次出现的
     * @param args
     * @param body
     * @param sb
     * @return
     */
    private String getRequestBody(Object[] args, String body,StringBuilder sb) {
        List<String> list = MyFastJsonUtil.parseArrays(body, String.class);
        for (int i = 0; i<args.length && !CollectionUtils.isEmpty(list); i++){
            Object arg = args[i];
            String needField = list.get(0);
            try {
                Field[] allFields = getAllFields(arg.getClass());
                for (Field field : allFields) {
                    if (field.getName().equals(needField)) {
                        field.setAccessible(true);
                        Object value = field.get(arg);
                        if(value instanceof Date){
                            value = ((Date)value).getTime();
                        }
                        sb.append(value.toString()).append("_");
                        list.remove(0);
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
               log.error("解析参数出错，参数为{}",arg);
            }
        }
        return sb.toString();
    }


    /**
     * 反射得到字段
     * @param aClass
     * @return
     */
    private static Field[] getAllFields(Class<?> aClass) {
        Field[] both = null;
        Field[] declaredFields = aClass.getDeclaredFields();
        Class<?> superclass = aClass.getSuperclass();
        List<Field> list = new ArrayList();
        while (superclass != null && superclass.getDeclaredFields() != null && superclass.getDeclaredFields().length > 0) {
            Field[] superclassDeclaredFields = superclass.getDeclaredFields();
            if (superclassDeclaredFields != null) {
                both = (Field[]) ArrayUtils.addAll(superclassDeclaredFields, declaredFields);
            }
            declaredFields = both;
            superclass = superclass.getSuperclass();
        }
        if (both == null) {
            both = declaredFields;
        }
        return both;
    }
}
