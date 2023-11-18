package com.github.example.spring.jedis.lock;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.util.Pool;

public class JedisPoolScriptCommand implements JedisScriptCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisPoolScriptCommand.class);

	private final Pool<?> pool;

	public JedisPoolScriptCommand(Pool<?> pool) {
		this.pool = pool;
	}

	@Override
	public Object eval(String script, int keyCount, String... params) {
		Jedis jedis = null;
		try {
			jedis = (Jedis)pool.getResource();
			if (Objects.nonNull(jedis)) {
				return jedis.eval(script, keyCount, params);
			}
		} finally {
			if (Objects.nonNull(jedis)) {
				jedis.close();
			}
		}
		return null;
	}

	@Override
	public String scriptLoad(String script) {
		Jedis jedis = null;
		try {
			jedis = (Jedis)pool.getResource();
			if (Objects.nonNull(jedis)) {
				return jedis.scriptLoad(script);
			}
		} finally {
			if (Objects.nonNull(jedis)) {
				jedis.close();
			}
		}
		return null;
	}

	@Override
	public Object evalsha(String sha, int keyCount, String... params) {
		Jedis jedis = null;
		try {
			jedis = (Jedis)pool.getResource();
			if (Objects.nonNull(jedis)) {
				return jedis.evalsha(sha, keyCount, params);
			}
		} finally {
			if (Objects.nonNull(jedis)) {
				jedis.close();
			}
		}
		return null;
	}

	@Override
	public void subscribe(Runnable callBack, JedisPubSub jedisPubSub, String... channels) {
		for (; ; ) {
			try {
				Jedis jedis = null;
				try {
					jedis = (Jedis)pool.getResource();
					if (Objects.nonNull(jedis)) {
						jedis.subscribe(jedisPubSub, channels);
					}
				} finally {
					if (Objects.nonNull(jedis)) {
						jedis.close();
					}
				}
			} catch (JedisConnectionException e) {
				callBack.run();
				try {
					TimeUnit.MILLISECONDS.sleep(DEFAULT_RETRY_SUBSCRIBE_TIME);
				} catch (InterruptedException ie) {
					LOGGER.error("Subscriber Interrupted", ie);
					Thread.currentThread().interrupt();
				}
			}
		}
	}
}
