package com.github.example.spring.jedis.lock.aspect;

import java.lang.reflect.Method;

import javax.annotation.Resource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import com.github.example.spring.jedis.lock.JedisLockManager;
import com.github.example.spring.jedis.lock.Lock;
import com.github.example.spring.jedis.lock.annotation.DistributedLock;
import com.github.example.spring.jedis.lock.exception.AcquireLockException;
import com.github.example.spring.jedis.lock.exception.JedisLockException;

@Aspect
@Component
public final class DistributedLockAspect {

	@Resource
	private JedisLockManager lockManager;

	@Pointcut("@annotation(com.github.example.spring.jedis.lock.annotation.DistributedLock)")
	public void interceptor() {
	}

	@Around("interceptor()")
	public Object before(ProceedingJoinPoint joinPoint) {
		final Class<?> targetClass = joinPoint.getTarget().getClass();
		final Class<?>[] types = ((MethodSignature)joinPoint.getSignature()).getParameterTypes();
		final String methodName = joinPoint.getSignature().getName();
		try {
			Method method = targetClass.getDeclaredMethod(methodName, types);
			if (method.isAnnotationPresent(DistributedLock.class)) {
				DistributedLock metadata = method.getAnnotation(DistributedLock.class);
				Lock lock = lockManager.getLock(metadata.name());
				try {
					if (DistributedLock.Method.LOCK.equals(metadata.method())) {
						lock.lock();
					} else if (DistributedLock.Method.TRY_LOCK.equals(metadata.method())) {
						long time = metadata.waitTime();
						boolean result = time < 1 ? lock.tryLock() : lock.tryLock(time, metadata.timeUnit());
						if (!result) {
							throw new AcquireLockException("Unable to acquire distributed lock");
						}
					}
					return joinPoint.proceed(joinPoint.getArgs());
				} finally {
					lock.unlock();
				}
			}
		} catch (Throwable e) {
			throw e instanceof AcquireLockException ? new JedisLockException("Try again", e) : new RuntimeException(e);
		}
		return null;
	}
}
