package chain.fxgj.server.payroll.config;

import chain.fxgj.core.common.constant.Constants;
import chain.fxgj.server.payroll.constant.PayrollConstants;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author chain
 * create by chain on 2018/8/10 下午2:50
 **/


@Configuration
@Slf4j
public class PayrollRedisConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(24))
                .computePrefixWith(
                        cacheName -> PayrollConstants.PREFIX.concat(":").concat(cacheName).concat(":"))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(keySerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer(objectMapper())));
        // 设置一个初始化的缓存空间set集合
        Set<String> cacheNames = new HashSet<>();
        cacheNames.add("loginSessionId");
        cacheNames.add("advertisement");  //广告位(轮播图)
        cacheNames.add("weixin");  //微信
        cacheNames.add("weixinOauth2");  //微信网页授权 获取code
        cacheNames.add("weixinOauth2AccessToken");  //微信网页授权 通过 code --> 获取 access_token
        cacheNames.add("weixinOauth2AccessTokenOpenid");  //微信网页授权 通过 access_token -->获取  用户信息
        cacheNames.add("weixinOauth2AccessTokenOpenid");  //微信网页授权 通过 access_token -->获取  用户信息
        cacheNames.add("empInfos");  //员工缓存信息
        cacheNames.add("checkFreePassword");  //免密查看工资标记


        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("loginSessionId", cacheConfiguration.entryTtl(Duration.ofMinutes(10)));
        configMap.put("advertisement", cacheConfiguration.entryTtl(Duration.ofMinutes(5)));
        configMap.put("weixin", cacheConfiguration.entryTtl(Duration.ofHours(2)));
        configMap.put("weixinOauth2", cacheConfiguration.entryTtl(Duration.ofMinutes(5)));
        configMap.put("weixinOauth2AccessToken", cacheConfiguration.entryTtl(Duration.ofSeconds(7200)));
        configMap.put("weixinOauth2AccessTokenOpenid", cacheConfiguration.entryTtl(Duration.ofSeconds(7200)));
        configMap.put("empInfos", cacheConfiguration.entryTtl(Duration.ofMinutes(2)));
        configMap.put("checkFreePassword", cacheConfiguration.entryTtl(Duration.ofMinutes(1)));;

        log.debug("自定义RedisCacheManager加载完成");
        return RedisCacheManager
                .builder(RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory))
                .initialCacheNames(cacheNames)
                .withInitialCacheConfigurations(configMap)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer(objectMapper()));
        redisTemplate.setHashValueSerializer(valueSerializer(objectMapper()));
        return redisTemplate;
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    private RedisSerializer<Object> valueSerializer(ObjectMapper objectMapper) {
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE);

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_TIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(Constants.DEFAULT_TIME_FORMAT)));
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(javaTimeModule);
        return objectMapper;
    }

}
