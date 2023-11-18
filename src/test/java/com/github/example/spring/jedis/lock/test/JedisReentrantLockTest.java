package com.github.example.spring.jedis.lock.test;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.example.spring.jedis.lock.JedisLockManager;
import com.github.example.spring.jedis.lock.Lock;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisReentrantLockTest {
	private volatile static Lock lock;

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
			System.out.println("Try acquire lock success");
			Thread.sleep(10000);
		} finally {
			System.out.println("Unlock success");
			lock.unlock();
		}
		try {
			Assert.assertTrue(lock.tryLock(1, TimeUnit.SECONDS));
			Thread.sleep(20000);
		} finally {
			System.out.println("Unlock success");
			lock.unlock();
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
