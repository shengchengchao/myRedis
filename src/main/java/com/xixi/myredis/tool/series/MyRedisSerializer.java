package com.xixi.myredis.tool.series;

import com.xixi.myredis.tool.util.MyFastJsonUtil;
import lombok.Data;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/23
 */
@Data
public class MyRedisSerializer<T> implements RedisSerializer<T> {



    Class<T> clazz;

    String typeName;
    /**
     * 主要是针对 map的类型 要获得 对应key 与value的类型
     */
    List<Class> classList;

    public MyRedisSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    public MyRedisSerializer() {
    }

    public MyRedisSerializer(Class<T> clazz, String typeName) {
        this.clazz = clazz;
        this.typeName = typeName;
    }

    /**
     * 进行序列化
     * @param t
     * @return
     * @throws SerializationException
     */
    @Override
    public byte[] serialize(T t) throws SerializationException {
        return MyFastJsonUtil.toJSONString(t).getBytes();
    }

    /**
     * 进行反序列化
     * @param bytes
     * @return
     * @throws SerializationException
     */
    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return bytes ==null ? null : MyFastJsonUtil.parseObject(new String(bytes),clazz) ;
    }
}
