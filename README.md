# myRedis
Redis的Spring中的二次封装
目前已自定义两个注解` @RedisSimpleCache`  与` @ZsetListCache` ，` @RedisSimpleCache`是用来存储一般的数据，` @ZsetListCache` 使用zset的结构，需要用来存储集合数据，支持分页查询
# Redis自定义注解@RedisSimpleCache

## 注解的作用

在使用过程中，不需要进行Redis中是否有数据的判断，就如同正常的查询数据库一般，当内容在数据库中不存在的情况下，会执行代码，在得到结果之后，再将数据存储中Redis中

## 注解的实现

使用Aop的技术，在方法中执行前查询数据，发现数据没有存在于数据库中，执行方法后，将数据存储中Redis的

### 具体的实现步骤

1. AOP拦截注解过的方法后，先去校验中key是否存在，
2. 存在的话，执行后续方法，判断方法返回的类型，来确定序列化以及反序列化的规则
3. 将Redis的key组装起来
4. 去redis中查询数据，判断数据有无
5. 在没有数据的情况下，就执行方法，将数据得到后，将结果保存到redis中
6. 在有数据的情况下，就直接返回Redis中数据即可。

## 具体实现

### 自定义注解

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RedisSimpleCache {

    /**
     * 过期时间 单位为秒
     * @return
     */
    int expire() default 86400;

    /**
     * redis 的key
     * @return
     */
    String redisKey() default "";

    /**
     * 在请求后是否刷新过期时间  true为刷新 false 为不刷新
     * @return
     */
    boolean reCalFlag() default false;
}
```

```java
/**只用来标记Key*/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RedisParamKey {

}
```

### AOP实现

```java
@Aspect
@Slf4j
@Component
public class SimpleCacheAopAdvice extends RedisBaseService {

    public static final String BLAKE = ":";

    public Random random;
    
    @Pointcut(value = "@annotation(com.xixi.myredis.tool.annotation.RedisSimpleCache)")
    public void test(){

    }

    public SimpleCacheAopAdvice() {
        this.random = new Random();
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
        //2.得到返回类型 来确定 序列化的规则 对于 List 与Map 要进行特别的数据，
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
            return read(joinPoint,sb.toString(),expire+random.nextInt(1000),reCalFlag,myRedisSerializer);
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

```

```java
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
        byte[] serialKey = RedisSerialUtils.serial(key);
        List redisResult = redisTemplate.executePipelined((RedisCallback) conn -> {
            byte[] bytes = conn.get(serialKey);
            return null;
        }, null);
        Random random =new Random();
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
                    redisTemplate.expire(serialKey,expire+random.nextInt(100), TimeUnit.SECONDS);
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
```



## 具体使用

只需要在方法中添加上两个注解就可以完成，将数据存储到Redis，再次访问后，将先去查询数据库中数据。

```java
@Service
public class TestService {

    @RedisSimpleCache(redisKey = "test",expire = 100)
    public String test(@RedisParamKey String k1 ){
        System.out.println("进入查询");
        return "aaa";
    }
}
```
