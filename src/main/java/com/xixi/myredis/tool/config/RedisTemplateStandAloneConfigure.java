package com.xixi.myredis.tool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author shengchengchao
 * @Description
 * @createTime 2021/3/16
 */
@Configuration
@EnableConfigurationProperties(RedisStandAloneProperties.class)
public class RedisTemplateStandAloneConfigure {

    @Autowired
    private RedisStandAloneProperties redisStandAloneProperties;

    @Bean(name = "jedisPoolConfig")
    public JedisPoolConfig getRedisConfig(){
        JedisPoolConfig config = new JedisPoolConfig();
        return config;
    }
    @Bean(name="connectionFactory")
    @Primary
    @ConditionalOnMissingBean
    public RedisConnectionFactory JedisConnectionFactory() {

        //reids单例模式,这个没有set方法注入到JedisConnectionFactory所以只能根据构造方法
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        //地址  端口  密码  数据库选择  spring认为这才是最基本的
        redisStandaloneConfiguration.setHostName(redisStandAloneProperties.getHostName());
        redisStandaloneConfiguration.setPort(redisStandAloneProperties.getPort());
        redisStandaloneConfiguration.setPassword(redisStandAloneProperties.getPassword());
        redisStandaloneConfiguration.setDatabase(redisStandAloneProperties.getDatabase());
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);

        return jedisConnectionFactory;
    }

    @Bean(name = "redisTemplate")
    @Primary
    public RedisTemplate redisTemplate(@Qualifier("connectionFactory") RedisConnectionFactory factory) {
        RedisTemplate redisTemplate = new RedisTemplate<>();
        RedisSerializer stringSerializer = new StringRedisSerializer();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.setKeySerializer(stringSerializer);
        redisTemplate.setValueSerializer(stringSerializer);
        redisTemplate.setHashKeySerializer(stringSerializer);
        redisTemplate.setHashValueSerializer(stringSerializer);
        return redisTemplate;
    }




}
