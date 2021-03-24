package com.xixi.myredis.tool.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/23
 */
public class MyFastJsonUtil {

    public static final SerializerFeature[] serializerFeatures =
            new SerializerFeature[]{ SerializerFeature.WriteMapNullValue,
                    SerializerFeature.QuoteFieldNames, SerializerFeature.WriteDateUseDateFormat};

    public static final Feature[] features = new Feature[]{};

    /**
     * 将对象写成 json字符串
     *
     * @param o
     * @return: 返回json
     */
    public static String toJSONString(Object o) {
        if(o==null){
            return null;
        }
        return JSON.toJSONString(o, serializerFeatures);
    }

    /**
     * json字符串还回对象数组
     *
     * @param  s
     * @param  clazz
     * @return: 返回对象
     */
    public static <T> List<T> parseArrays(String s, Class<T> clazz){
        return JSON.parseArray(s, clazz);
    }

    /**
     * json字符串还回对象
     * <p>
     * 如：<br/>
     * String strJson="{\"name\":1}";<br/>
     * HashMap&lt;?, ?&gt map=FastJsonUtil.parseObject(strJson, HashMap.class);
     * </p>
     *
     * @param  text
     * @param  clazz
     * @return: 返回对象
     */
    public static <T> T parseObject(String text, Class<T> clazz){
        if(text==null){
            return null;
        }
        return JSON.parseObject(text, clazz);
    }


    /**
     * 解析成map
     * @param path
     * @param k
     * @param v
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> Map<K, V> json2Map(String path, Class<K> k, Class<V> v) {
        return JSON.parseObject(path, new TypeReference<Map<K, V>>(k, v) {
        });
    }
}
