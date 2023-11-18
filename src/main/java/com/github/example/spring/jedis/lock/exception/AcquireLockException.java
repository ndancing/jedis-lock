package com.github.example.spring.jedis.lock.exception;

public class AcquireLockException extends JedisLockException {

	private static final long serialVersionUID = 8489993434666410845L;

	public AcquireLockException(String message) {
		super(message);
	}

	public AcquireLockException(Throwable e) {
		super(e);
	}

	public AcquireLockException(String message, Throwable cause) {
		super(message, cause);
	}
}
