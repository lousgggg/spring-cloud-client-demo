package com.wiley.luo.springcloudclientdemo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtil {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    
    // ============================== common ==============================
    
    /**
     * 设置缓存过期时间
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 获取过期时间
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
    
    /**
     * 判断key是否存在
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 删除缓存
     */
    public void delete(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }
    
    /**
     * 模糊删除
     */
    public void deleteByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
    
    // ============================== String ==============================
    
    /**
     * 普通缓存获取
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }
    
    /**
     * 普通缓存放入
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 普通缓存放入并设置时间
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 递增
     */
    public long increment(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    /**
     * 递减
     */
    public long decrement(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().decrement(key, -delta);
    }
    
    // ============================== Hash ==============================
    
    /**
     * HashGet
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }
    
    /**
     * 获取hashKey对应的所有键值
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }
    
    /**
     * HashSet
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * HashSet 并设置时间
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ============================== Set ==============================
    
    /**
     * 根据key获取Set中的所有值
     */
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将数据放入set缓存
     */
    public long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    // ============================== List ==============================
    
    /**
     * 获取list缓存的内容
     */
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 将list放入缓存
     */
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // ============================== 分布式锁 ==============================
    
    /**
     * 获取分布式锁
     */
    public boolean tryLock(String key, String value, long expireTime) {
        Boolean result = redisTemplate.execute((RedisCallback<Boolean>) connection -> {
            RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
            byte[] keyBytes = serializer.serialize(key);
            byte[] valueBytes = serializer.serialize(value);
            
            // 使用SET命令的NX选项（如果key不存在才设置）
            Boolean success = connection.stringCommands()
                .set(keyBytes, valueBytes, 
                     Expiration.seconds(expireTime),
                     RedisStringCommands.SetOption.SET_IF_ABSENT);
            return Boolean.TRUE.equals(success);
        });
        return Boolean.TRUE.equals(result);
    }
    
    /**
     * 释放分布式锁
     */
    public boolean releaseLock(String key, String value) {
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                          "return redis.call('del', KEYS[1]) " +
                          "else return 0 end";
        
        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), value);
        return result != null && result == 1;
    }
    
    // ============================== 限流 ==============================
    
    /**
     * 滑动窗口限流
     */
    public boolean isActionAllowed(String key, int period, int maxCount) {
        long now = System.currentTimeMillis();
        
        BoundZSetOperations<String, Object> zSetOps = redisTemplate.boundZSetOps(key);
        
        // 移除窗口外的数据
        zSetOps.removeRangeByScore(0, now - period * 1000L);
        
        // 获取当前窗口内的请求数量
        Long count = zSetOps.zCard();
        
        if (count < maxCount) {
            // 添加当前请求
            zSetOps.add(UUID.randomUUID().toString(), now);
            // 设置过期时间
            zSetOps.expire(period + 1, TimeUnit.SECONDS);
            return true;
        }
        
        return false;
    }
}