package com.github.example.spring.jedis.lock.test;

import com.github.example.spring.jedis.lock.JedisLockManager;
import com.github.example.spring.jedis.lock.Lock;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;

public class JedisRedLockBenchmark extends LockBenchmark {
    private static volatile Lock lock;

    @BeforeClass
    public static void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(20);
        config.setMaxIdle(50);
        config.setMaxTotal(100);
        config.setMaxWaitMillis(2000);
        lock = new JedisLockManager(Arrays.asList(
                new JedisPool(config, "127.0.0.1", 6379),
                new JedisPool(config, "127.0.0.1", 6380),
                new JedisPool(config, "127.0.0.1", 6381))
        ).getLock("red_lock");
    }

    @Test
    public void lock() {
        benchmark(() -> {
            try {
                lock.lock();
                Thread.sleep(TASK_TIME);
            } catch (InterruptedException e) {
            } finally {
                lock.unlock();
            }
        });
    }

    @Test
    public void tryLock() {
        benchmark(() -> {
            if (lock.tryLock()) {
                try {
                    Thread.sleep(TASK_TIME);
                } catch (InterruptedException e) {
                } finally {
                    lock.unlock();
                }
            }
        });
    }
}
