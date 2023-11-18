package com.github.example.spring.jedis.lock;

import java.util.Collection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.example.spring.jedis.lock.exception.JedisLockException;

public class JedisRedLock extends JedisMultiLock {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisRedLock.class);

	public JedisRedLock(Collection<Lock> internalLocks, JedisLockConfigs configs) {
		super(internalLocks, configs);
	}

	@Override
	protected void unlockInternal(Collection<Lock> locks) {
		Objects.requireNonNull(locks);
		locks.forEach(lock -> {
			try {
				lock.unlock();
			} catch (JedisLockException e) {
				LOGGER.error(e.getMessage(), e);
			}
		});
	}

	@Override
	protected long getLockWaitTime(long remainTime) {
		return Math.max(remainTime / internalLocks.size(), 1);
	}

	@Override
	protected int failedLocksLimit() {
		return internalLocks.size() - ((internalLocks.size() >> 1) + 1);
	}
}
