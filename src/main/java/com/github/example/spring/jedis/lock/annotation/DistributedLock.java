package com.github.example.spring.jedis.lock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {

	enum KeyType {
		CONSTANT,
		ARGUMENTS
	}

	KeyType keyType() default KeyType.CONSTANT;

	String keyFormat();

	long waitTime() default 0L;

	TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

	Method method() default Method.LOCK;

	enum Method {
		LOCK,
		TRY_LOCK
	}

}
