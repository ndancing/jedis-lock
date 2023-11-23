package com.github.example.spring.jedis.lock.test;

import com.github.example.spring.jedis.lock.JedisLockManager;
import com.github.example.spring.jedis.lock.Lock;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.TimeUnit;

public class JedisReentrantLockTest {
	private static Lock lock;

	@BeforeClass
	public static void init() {
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMinIdle(10);
		config.setMaxIdle(50);
		config.setMaxTotal(100);
		config.setMaxWaitMillis(1000);
		lock = new JedisLockManager(new JedisPool(config, "127.0.0.1", 6379)).getLock("reentrant_lock");
	}

	@Test
	public void lock() throws InterruptedException {
		try {
			lock.lock();
			System.out.println("Acquire lock success");
			Thread.sleep(100000);
		} finally {
			lock.unlock();
			System.out.println("Unlock success");
		}
	}

	@Test
	public void tryLock() throws InterruptedException {
		try {
			Assert.assertTrue(lock.tryLock());
			System.out.println("1st Try acquire lock success");
			Thread.sleep(10000);
		} finally {
			lock.unlock();
			System.out.println("1st Unlock success");
		}
		try {
			Assert.assertTrue(lock.tryLock(1, TimeUnit.SECONDS));
			System.out.println("2nd Try acquire lock success");
			Thread.sleep(20000);
		} finally {
			lock.unlock();
			System.out.println("2nd Unlock success");
		}
	}

	@Test
	public void forceUnlock() throws InterruptedException {
		try {
			lock.tryLock();
			System.out.println("Try acquire lock success");
			Thread.sleep(60000);
		} finally {
			lock.forceUnlock();
			System.out.println("Force unlock success");
		}
	}
}
