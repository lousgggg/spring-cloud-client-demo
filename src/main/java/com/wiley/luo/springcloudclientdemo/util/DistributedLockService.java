package com.wiley.luo.springcloudclientdemo.util;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@Slf4j
public class DistributedLockService {
    
    @Autowired
    private RedissonClient redissonClient;
    
    /**
     * 基础加锁方法
     * 
     * @param lockKey 锁的key
     * @param waitTime 获取锁的最大等待时间
     * @param leaseTime 锁的持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断, lockKey: {}", lockKey, e);
            return false;
        }
    }
    
    /**
     * 释放锁
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.debug("释放分布式锁: {}", lockKey);
        }
    }
    
    /**
     * 执行带锁的业务逻辑（自动获取和释放锁）
     * 
     * @param lockKey 锁key
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param unit 时间单位
     * @param supplier 业务逻辑
     * @return 业务执行结果
     */
    public <T> T executeWithLock(String lockKey, 
                                long waitTime, 
                                long leaseTime, 
                                TimeUnit unit, 
                                Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;
        
        try {
            // 尝试获取锁
            locked = lock.tryLock(waitTime, leaseTime, unit);
            
            if (locked) {
                log.debug("成功获取分布式锁: {}", lockKey);
                // 执行业务逻辑
                return supplier.get();
            } else {
                log.warn("获取分布式锁失败: {}, 可能被其他实例占用", lockKey);
                throw new RuntimeException("获取锁失败，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        } finally {
            if (locked) {
                lock.unlock();
                log.debug("释放分布式锁: {}", lockKey);
            }
        }
    }
    
    /**
     * 执行带锁的业务逻辑（无返回值）
     */
    public void executeWithLock(String lockKey, 
                               long waitTime, 
                               long leaseTime, 
                               TimeUnit unit, 
                               Runnable runnable) {
        executeWithLock(lockKey, waitTime, leaseTime, unit, () -> {
            runnable.run();
            return null;
        });
    }
    
    /**
     * 可重入锁计数
     */
    public int getLockHoldCount(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.getHoldCount();
    }
    
    /**
     * 检查锁是否被当前线程持有
     */
    public boolean isHeldByCurrentThread(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return lock.isHeldByCurrentThread();
    }
    
    /**
     * 强制释放锁（危险操作，慎用）
     */
    public void forceUnlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.forceUnlock();
        log.warn("强制释放分布式锁: {}", lockKey);
    }
}