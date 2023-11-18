package com.github.example.spring.jedis.lock;

import java.util.Set;

import redis.clients.jedis.JedisPubSub;

abstract class SubscribeListener extends JedisPubSub {

	protected final Set<Thread> subscribers;

	protected SubscribeListener(Set<Thread> subscribers) {
		this.subscribers = subscribers;
	}
}
