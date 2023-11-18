package com.github.example.spring.jedis.lock;

import java.util.concurrent.TimeUnit;

public interface Lock {

	void lock();

	boolean tryLock();

	boolean tryLock(long time, TimeUnit unit);

	void unlock();

	void forceUnlock();

}
