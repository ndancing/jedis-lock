package com.github.example.spring.jedis.lock;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPubSub;

public class JedisClusterScriptCommand implements JedisScriptCommand {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisClusterScriptCommand.class);

	private final JedisCluster cluster;

	public JedisClusterScriptCommand(JedisCluster cluster) {
		this.cluster = cluster;
	}

	@Override
	public Object eval(String script, int keyCount, String... params) {
		return cluster.eval(script, keyCount, params);
	}

	@Override
	public String scriptLoad(String script) {
		return cluster.scriptLoad(script, script);
	}

	@Override
	public Object evalsha(String sha, int keyCount, String... params) {
		return cluster.evalsha(sha, keyCount, params);
	}

	@Override
	public void subscribe(Runnable callBack, JedisPubSub jedisPubSub, String... channels) {
		for (; ; ) {
			try {
				cluster.subscribe(jedisPubSub, channels);
			} catch (Exception e) {
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
