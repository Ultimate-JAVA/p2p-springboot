package com.gz.p2p.service.impl.user;

import com.alibaba.dubbo.config.annotation.Service;
import com.gz.p2p.service.user.RedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/13/013 18:17
 * @Description:
 */
@Service(interfaceClass = RedisService.class,version = "1.0.0",timeout = 50000)
@Component
public class RedisServiceImpl implements RedisService {
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public void put(Object key, Object value) {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.opsForValue().set(key, value,30, TimeUnit.MINUTES);
    }

    @Override
    public String get(Object key) {

        return (String) redisTemplate.opsForValue().get(key);
    }
}
