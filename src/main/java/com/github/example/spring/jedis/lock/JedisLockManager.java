package com.github.example.spring.jedis.lock;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.springframework.util.Assert;

import com.github.example.spring.jedis.lock.enums.RedisSetup;
import com.github.example.spring.jedis.lock.exception.AcquireLockException;
import redis.clients.jedis.JedisCluster;
import redis.clients.util.Pool;

public class JedisLockManager {

	private final Map<String, Lock> poolLock = new ConcurrentHashMap<>(32);
	private RedisSetup redisSetup;
	private JedisCluster jedisCluster;
	private List<Pool<?>> pools;
	private JedisLockConfigs configs;
	private Executor executor;
	private ScheduledExecutorService scheduler;

	public JedisLockManager(Pool<?> pool) {
		this(pool, JedisLockConfigs.builder().build());
	}

	public JedisLockManager(Pool<?> pool, JedisLockConfigs configs) {
		this(configs);
		Assert.notNull(pool, "Invalid Jedis connection config");
		this.pools = Collections.singletonList(pool);
		this.redisSetup = RedisSetup.SINGLE;
	}

	public JedisLockManager(JedisCluster jedisCluster) {
		this(jedisCluster, JedisLockConfigs.builder().build());
	}

	public JedisLockManager(JedisCluster jedisCluster, JedisLockConfigs configs) {
		this(configs);
		Assert.notNull(jedisCluster, "Invalid Jedis cluster config");
		this.jedisCluster = jedisCluster;
		this.redisSetup = RedisSetup.CLUSTER;
	}

	public JedisLockManager(List<Pool<?>> pools) {
		this(pools, JedisLockConfigs.builder().build());
	}

	public JedisLockManager(List<Pool<?>> pools, JedisLockConfigs configs) {
		this(configs);
		Assert.notEmpty(pools, "Invalid Jedis connections config");
		this.pools = pools;
		this.redisSetup = RedisSetup.RED;
	}

	protected JedisLockManager(JedisLockConfigs configs) {
		this.configs = configs;
		this.executor = new ThreadPoolExecutor(configs.getWorkerCorePoolSize(), configs.getWorkerMaxPoolSize(), configs.getWorkerThreadAliveTime(),
			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(configs.getWorkerQueueSize()), new ThreadPoolExecutor.AbortPolicy());
		this.scheduler = Executors.newScheduledThreadPool(configs.getSchedulerCorePoolSize());
	}

	public Lock getLock(String name) {
		Objects.requireNonNull(name);
		Lock result;
		synchronized (this) {
			result = poolLock.get(name);
			if (Objects.isNull(result)) {
				switch (redisSetup) {
					case SINGLE:
						result = new JedisReentrantLock(name, configs, new JedisPoolScriptCommand(pools.get(0)), executor, scheduler);
						break;
					case CLUSTER:
						result = new JedisReentrantLock(name, configs, new JedisClusterScriptCommand(jedisCluster), executor, scheduler);
						break;
					case RED:
						final List<Lock> internalLocks = new LinkedList<>();
						pools.forEach(
							pool -> internalLocks.add(new JedisReentrantLock(name, configs, new JedisPoolScriptCommand(pool), executor, scheduler)));
						result = new JedisRedLock(Collections.unmodifiableList(internalLocks), configs);
						break;
					default:
						throw new AcquireLockException("Unable to acquire lock with name: " + name);
				}
				poolLock.put(name, result);
			}
		}
		return result;
	}
}
