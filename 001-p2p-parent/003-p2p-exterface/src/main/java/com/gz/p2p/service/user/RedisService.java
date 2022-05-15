package com.gz.p2p.service.user;

/**
 * @Auther: 翟文海
 * @Date: 2022/5/13/013 18:17
 * @Description:
 */
public interface RedisService {
    void put(Object key, Object value);

    Object get(Object key);

}
