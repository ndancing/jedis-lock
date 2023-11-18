package com.github.example.spring.jedis.lock;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.example.spring.jedis.lock.exception.JedisLockException;

public abstract class JedisMultiLock implements Lock {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisMultiLock.class);

	protected final Collection<Lock> internalLocks;
	protected final JedisLockConfigs configs;

	protected JedisMultiLock(Collection<Lock> internalLocks, JedisLockConfigs configs) {
		this.internalLocks = internalLocks;
		this.configs = configs;
	}

	@Override
	public void lock() {
		Objects.requireNonNull(internalLocks);
		final long waitingTime = internalLocks.size() * (configs.getMultiLockWaitingTime() > 0 ? configs.getMultiLockWaitingTime() :
			JedisLockConfigs.DEFAULT_MULTI_LOCK_WAITING_TIME);
		while (true) {
			if (tryLock(waitingTime, TimeUnit.MILLISECONDS)) {
				return;
			}
		}
	}

	@Override
	public boolean tryLock() {
		return tryLock(-1, TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		if (internalLocks.size() < 3) {
			throw new JedisLockException("Required more than 3 redis instances");
		}
		long beginTime = System.currentTimeMillis();
		long remainTime = time != -1L ? unit.toMillis(time) : -1L;
		long lockTime = getLockWaitTime(remainTime);
		AtomicInteger acquiredLocks = new AtomicInteger();
		internalLocks.stream().filter(Objects::nonNull).forEach(lock -> {
			boolean result;
			try {
				result = time == -1L ? lock.tryLock() : lock.tryLock(lockTime, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				result = false;
			}
			if (result) {
				acquiredLocks.incrementAndGet();
			}
		});

		if (acquiredLocks.get() >= (internalLocks.size() - failedLocksLimit())) {
			long endTime = System.currentTimeMillis() - beginTime;
			if (remainTime != -1L && (remainTime - endTime) <= 0L) {
				unlockInternal(internalLocks);
				return false;
			}
			return true;
		} else {
			unlockInternal(internalLocks);
			return false;
		}
	}

	@Override
	public void unlock() {
		internalLocks.forEach(lock -> {
			try {
				lock.unlock();
			} catch (JedisLockException e) {
				LOGGER.error(e.getMessage(), e);
			}
		});
	}

	@Override
	public void forceUnlock() {
		internalLocks.forEach(lock -> {
			try {
				lock.forceUnlock();
			} catch (JedisLockException e) {
				LOGGER.error(e.getMessage(), e);
			}
		});
	}

	protected abstract void unlockInternal(Collection<Lock> locks);

	protected abstract long getLockWaitTime(long remainTime);

	protected abstract int failedLocksLimit();
}
