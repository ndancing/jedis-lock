package com.github.example.spring.jedis.lock;

import java.util.Set;
import java.util.concurrent.locks.LockSupport;

final class JedisLockSubscribeListener extends SubscribeListener {

	JedisLockSubscribeListener(Set<Thread> subscribers) {
		super(subscribers);
	}

	@Override
	public void onMessage(String channel, String message) {
		synchronized (subscribers) {
			subscribers.stream().forEach(s -> LockSupport.unpark(s));
		}
	}
}
