package com.xixi.myredis.tool.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/24
 */

public class RedisUtils {


    //标志最终存储中，数据为空
    protected byte[] emptyByte=new byte[]{0};

    @Autowired
    protected static RedisTemplate redisTemplate;



    /**
     * 移除redis中的缓存
     */
    public static void destroyRedis(Long key) {
        redisTemplate.execute((RedisCallback) conn->{
            conn.del(serial(key));
            return null;
        });
    }

    /**
     * 移除redis中的缓存
     */
    public static void destroyRedis(String key) {
        redisTemplate.execute((RedisCallback) conn->{
            conn.del(serial(key));
            return null;
        });
    }


    public void destroyRedisPipeLined(Long key, RedisConnection conn) {
        conn.del(serial(key));
    }

    /**
     * 移除redis中的缓存
     */
    public static boolean exists(Long key) {
        return (boolean) redisTemplate.execute((RedisCallback) conn->{
            return conn.exists(serial(key));
        });
    }




    private static Charset charset=Charset.forName("UTF8");

    public static byte[] serial(String str) {
        return str.getBytes(charset);
    }



    public static byte[] serial(Object o){
        if(o==null) {
            return null;
        }
        if(o instanceof Long||o instanceof Integer||o instanceof String || o instanceof Double){
            return serial(o.toString());
        }else {
            return MyFastJsonUtil.toJSONString(o).getBytes();
        }
    }



    public static String deSerialString(byte[] data) {
        return (data == null ? null : new String(data, charset));
    }

    public static Long deSerialLong(byte[] data) {
        return (data == null ? null : Long.valueOf(new String(data, charset)));
    }

    //超时时间，返回-1时，表示永不超时
    public static int expireTime(){ return 86400;}
}
