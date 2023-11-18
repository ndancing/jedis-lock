package com.github.example.spring.jedis.lock;

import redis.clients.jedis.JedisPubSub;

public interface JedisScriptCommand extends ScriptCommand {

	int DEFAULT_RETRY_SUBSCRIBE_TIME = 1000;

	void subscribe(Runnable callBack, JedisPubSub jedisPubSub, String... channels);
}
