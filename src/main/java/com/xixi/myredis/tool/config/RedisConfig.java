//package com.xixi.myredis.tool.config;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisNode;
//import org.springframework.data.redis.connection.RedisSentinelConfiguration;
//import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.RedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//import redis.clients.jedis.JedisPoolConfig;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
///**
// * @author shengchengchao
// * @Description
// * @createTime 2021/3/18
// */
//@Configuration
//@EnableAutoConfiguration
//public class RedisConfig {
//    private static Logger logger = LoggerFactory.getLogger(RedisConfig.class);
//
//    @Value("#{'${spring.redis.sentinel.nodes}'.split(',')}")
//    private List<String> nodes;
//
//
//    @Bean(name = "jedisPoolConfig")
//    @ConfigurationProperties(prefix="spring.redis.jedis.pool")
//    public JedisPoolConfig getRedisConfig(){
//        JedisPoolConfig config = new JedisPoolConfig();
//        return config;
//    }
//
//    @Bean(name = "sentinelConfiguration")
//    public RedisSentinelConfiguration sentinelConfiguration(){
//        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
//        //??????matser?????????
//        redisSentinelConfiguration.master("mymaster");
//        redisSentinelConfiguration.setPassword("admin");
//        //??????redis?????????sentinel
//        Set<RedisNode> redisNodeSet = new HashSet<>();
//        nodes.forEach(x->{
//            redisNodeSet.add(new RedisNode(x.split(":")[0],Integer.parseInt(x.split(":")[1])));
//        });
//        logger.info("redisNodeSet -->"+redisNodeSet);
//        redisSentinelConfiguration.setSentinels(redisNodeSet);
//        return redisSentinelConfiguration;
//    }
//
//    @Bean(name = "jedisConnectionFactory")
//    public JedisConnectionFactory jedisConnectionFactory(@Qualifier("jedisPoolConfig")JedisPoolConfig jedisPoolConfig,@Qualifier("sentinelConfiguration") RedisSentinelConfiguration sentinelConfig) {
//        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(sentinelConfig,jedisPoolConfig);
//        return jedisConnectionFactory;
//    }
//
//    @Bean(name = "redisTemplate")
//    @Primary
//    @ConditionalOnMissingBean
//    public RedisTemplate redisTemplate(@Qualifier("jedisConnectionFactory") RedisConnectionFactory factory) {
//        RedisTemplate redisTemplate = new RedisTemplate<>();
//        RedisSerializer stringSerializer = new StringRedisSerializer();
//        redisTemplate.setConnectionFactory(factory);
//        redisTemplate.setKeySerializer(stringSerializer);
//        redisTemplate.setValueSerializer(stringSerializer);
//        redisTemplate.setHashKeySerializer(stringSerializer);
//        redisTemplate.setHashValueSerializer(stringSerializer);
//        return redisTemplate;
//    }
//}
