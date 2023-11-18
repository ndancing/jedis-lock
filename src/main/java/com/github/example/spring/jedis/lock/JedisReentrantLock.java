package com.github.example.spring.jedis.lock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.example.spring.jedis.lock.exception.JedisLockException;

public class JedisReentrantLock extends JedisScriptCommandLock {

	private static final Logger LOGGER = LoggerFactory.getLogger(JedisReentrantLock.class);

	private static final Long DEFAULT_RESPONSE_TTL = 100L;

	private final String name;
	private final JedisLockConfigs configs;
	private final Set<Thread> subscribers = Collections.synchronizedSet(new HashSet<>());
	private final Executor subscribeExecutor;
	private final ScheduledExecutorService watchDogScheduler;

	private ThreadLocal<String> threadLocal = new ThreadLocal<>();
	private boolean hasListener;
	private Future<?> watchingProcess;

	/*
	 * Lazy load scripts
	 *  */
	private Script lockScript;
	private Script unLockScript;
	private Script forceUnLockScript;
	private Script updateTTLScript;

	public JedisReentrantLock(String name, JedisLockConfigs configs, JedisScriptCommand scriptCommand, Executor executor,
		ScheduledExecutorService scheduler) {
		super(scriptCommand);
		this.name = name;
		this.configs = configs;
		this.subscribeExecutor = executor;
		this.watchDogScheduler = scheduler;
	}

	@Override
	public void lock() {
		lock(acquireVisitorId());
	}

	@Override
	public boolean tryLock() {
		return tryLock(acquireVisitorId());
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		return tryLock(acquireVisitorId(), time, unit);
	}

	@Override
	public void unlock() {
		unlock(acquireVisitorId());
	}

	@Override
	public void forceUnlock() {
		synchronized (this) {
			if (Objects.isNull(forceUnLockScript)) {
				final String forceUnlockScriptContent = JedisLockScript.getScriptFromFilePath(ScriptFilePaths.FORCE_UNLOCK_SCRIPT);
				forceUnLockScript = new JedisLockScript(forceUnlockScriptContent, scriptCommand.scriptLoad(forceUnlockScriptContent));
			}
		}
		Long result = null;
		try {
			result = executeScriptCommand(forceUnLockScript, 1, name);
		} catch (ClassCastException e) {
			LOGGER.error("Class casting error", e);
		} finally {
			if (result == 1 && Objects.nonNull(watchingProcess)) {
				watchingProcess.cancel(true);
			}
		}
	}

	@Override
	protected void subscribe() {
		Thread thread = Thread.currentThread();
		synchronized (subscribers) {
			if (!hasListener) {
				hasListener = true;
				subscribeExecutor.execute(() -> scriptCommand.subscribe(() -> {
					if (Objects.nonNull(watchingProcess)) {
						watchingProcess.cancel(true);
					}
				}, new JedisLockSubscribeListener(subscribers), name));
			}
			subscribers.add(thread);
		}
	}

	@Override
	protected void unsubscribe() {
		Thread thread = Thread.currentThread();
		synchronized (subscribers) {
			if (subscribers.contains(thread)) {
				subscribers.remove(thread);
			}
		}
	}

	private void lock(String visitorId) {
		Long ttl = acquireLock(visitorId);
		if (ttl == -1) {
			watchDog(visitorId);
			return;
		}
		subscribe();
		try {
			while (true) {
				ttl = acquireLock(visitorId);
				if (ttl == -1) {
					watchDog(visitorId);
					break;
				}
				if (ttl >= 0) {
					LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(ttl));
				}
			}
		} finally {
			unsubscribe();
		}
	}

	private boolean tryLock(String visitorId) {
		boolean result = false;
		if (acquireLock(visitorId) == -1) {
			watchDog(visitorId);
			result = true;
		}
		return result;
	}

	private boolean tryLock(String visitorId, long time, TimeUnit unit) {
		Objects.requireNonNull(unit);
		if (time < 0) {
			throw new JedisLockException("time parameter must be >= 0");
		}
		Long ttl = acquireLock(visitorId);
		if (ttl == -1) {
			watchDog(visitorId);
			return true;
		}
		subscribe();
		try {
			LockSupport.parkNanos(unit.toNanos(time));
			ttl = acquireLock(visitorId);
			if (ttl == -1) {
				watchDog(visitorId);
				return true;
			}
		} finally {
			unsubscribe();
		}
		return false;
	}

	private void unlock(String visitorId) {
		synchronized (this) {
			if (Objects.isNull(unLockScript)) {
				final String unlockScriptContent = JedisLockScript.getScriptFromFilePath(ScriptFilePaths.UNLOCK_SCRIPT);
				unLockScript = new JedisLockScript(unlockScriptContent, scriptCommand.scriptLoad(unlockScriptContent));
			}
		}
		Long result = null;
		try {
			result = executeScriptCommand(unLockScript, 1, name, visitorId, String.valueOf(configs.getKeyTTL()));
		} finally {
			if (Objects.isNull(result)) {
				threadLocal.remove();
				if (Objects.nonNull(watchingProcess)) {
					watchingProcess.cancel(true);
				}
			} else if (result == 1) {
				if (Objects.nonNull(watchingProcess)) {
					watchingProcess.cancel(true);
				}
			} else if (result == 0) {
				throw new JedisLockException("Not locked by current thread with visitorId=" + visitorId);
			}
		}
	}

	private Long acquireLock(String visitorId) {
		synchronized (this) {
			if (Objects.isNull(lockScript)) {
				final String lockScriptContent = JedisLockScript.getScriptFromFilePath(ScriptFilePaths.LOCK_SCRIPT);
				lockScript = new JedisLockScript(lockScriptContent, scriptCommand.scriptLoad(lockScriptContent));
			}
		}
		try {
			return executeScriptCommand(lockScript, 1, name, visitorId, String.valueOf(configs.getKeyTTL()));
		} catch (ClassCastException e) {
			LOGGER.error("Class casting error", e);
			return DEFAULT_RESPONSE_TTL;
		}
	}

	private String acquireVisitorId() {
		String visitorId = threadLocal.get();
		if (Objects.isNull(visitorId)) {
			visitorId = String.format("%s:%s", UUID.randomUUID().toString(), Thread.currentThread().getId());
			threadLocal.set(visitorId);
		}
		return visitorId;
	}

	private void watchDog(String visitorId) {
		if (Objects.nonNull(watchingProcess)) {
			watchingProcess.cancel(true);
		}
		watchingProcess = watchDogScheduler.scheduleAtFixedRate(() -> {
			if (Objects.isNull(updateTTLScript)) {
				final String updateTTLScriptContent = JedisLockScript.getScriptFromFilePath(ScriptFilePaths.UPDATE_TTL_SCRIPT);
				updateTTLScript = new JedisLockScript(updateTTLScriptContent, scriptCommand.scriptLoad(updateTTLScriptContent));
			}
			executeScriptCommand(updateTTLScript, 1, name, visitorId, String.valueOf(configs.getKeyTTL()));
		}, configs.getUpdateTTLScheduleTime(), configs.getUpdateTTLScheduleTime(), TimeUnit.MILLISECONDS);
	}
}
