package com.github.example.spring.jedis.lock;

import org.springframework.util.Assert;

public class JedisLockConfigs {

	private JedisLockConfigs() {
	}

	public static final int DEFAULT_KEY_TTL = 30000;
	public static final int DEFAULT_UPDATE_TTL_SCHEDULE_TIME = 10000;
	public static final int DEFAULT_WORKER_CORE_POOL_SIZE = 10;
	public static final int DEFAULT_WORKER_MAX_POOL_SIZE = 200;
	public static final int DEFAULT_WORKER_THREAD_ALIVE_TIME = 2000;
	public static final int DEFAULT_WORKER_QUEUE_SIZE = 1000;
	public static final int DEFAULT_SCHEDULER_CORE_POOL_SIZE = 10;
	public static final int DEFAULT_MULTI_LOCK_WAITING_TIME = 1500;

	private int keyTTL;
	private int updateTTLScheduleTime;
	private int workerCorePoolSize;
	private int workerMaxPoolSize;
	private int workerThreadAliveTime;
	private int workerQueueSize;
	private int schedulerCorePoolSize;
	private int multiLockWaitingTime;

	protected JedisLockConfigs(Builder builder) {
		this.keyTTL = builder.keyTTL;
		this.updateTTLScheduleTime = builder.updateTTLScheduleTime;
		this.workerCorePoolSize = builder.workerCorePoolSize;
		this.workerMaxPoolSize = builder.workerMaxPoolSize;
		this.workerThreadAliveTime = builder.workerThreadAliveTime;
		this.workerQueueSize = builder.workerQueueSize;
		this.schedulerCorePoolSize = builder.schedulerCorePoolSize;
		this.multiLockWaitingTime = builder.multiLockWaitingTime;
	}

	public static Builder builder() {
		return new Builder();
	}

	public int getKeyTTL() {
		return keyTTL;
	}

	public int getUpdateTTLScheduleTime() {
		return updateTTLScheduleTime;
	}

	public int getWorkerCorePoolSize() {
		return workerCorePoolSize;
	}

	public int getWorkerMaxPoolSize() {
		return workerMaxPoolSize;
	}

	public int getWorkerThreadAliveTime() {
		return workerThreadAliveTime;
	}

	public int getWorkerQueueSize() {
		return workerQueueSize;
	}

	public int getSchedulerCorePoolSize() {
		return schedulerCorePoolSize;
	}

	public int getMultiLockWaitingTime() {
		return multiLockWaitingTime;
	}

	public static class Builder {

		private int keyTTL = DEFAULT_KEY_TTL;
		private int updateTTLScheduleTime = DEFAULT_UPDATE_TTL_SCHEDULE_TIME;
		private int workerCorePoolSize = DEFAULT_WORKER_CORE_POOL_SIZE;
		private int workerMaxPoolSize = DEFAULT_WORKER_MAX_POOL_SIZE;
		private int workerThreadAliveTime = DEFAULT_WORKER_THREAD_ALIVE_TIME;
		private int workerQueueSize = DEFAULT_WORKER_QUEUE_SIZE;
		private int schedulerCorePoolSize = DEFAULT_SCHEDULER_CORE_POOL_SIZE;
		private int multiLockWaitingTime = DEFAULT_MULTI_LOCK_WAITING_TIME;

		public Builder keyTTL(int keyTTL) {
			this.keyTTL = keyTTL;
			return this;
		}

		public Builder updateTTLScheduleTime(int updateTTLScheduleTime) {
			this.updateTTLScheduleTime = updateTTLScheduleTime;
			return this;
		}

		public Builder workerCorePoolSize(int workerCorePoolSize) {
			this.workerCorePoolSize = workerCorePoolSize;
			return this;
		}

		public Builder workerMaxPoolSize(int workerMaxPoolSize) {
			this.workerMaxPoolSize = workerMaxPoolSize;
			return this;
		}

		public Builder workerThreadAliveTime(int workerThreadAliveTime) {
			this.workerThreadAliveTime = workerThreadAliveTime;
			return this;
		}

		public Builder workerQueueSize(int workerQueueSize) {
			this.workerQueueSize = workerQueueSize;
			return this;
		}

		public Builder schedulerCorePoolSize(int schedulerCorePoolSize) {
			this.schedulerCorePoolSize = schedulerCorePoolSize;
			return this;
		}

		public Builder multiLockWaitingTime(int multiLockWaitingTime) {
			this.multiLockWaitingTime = multiLockWaitingTime;
			return this;
		}

		public JedisLockConfigs build() {
			Assert.isTrue(keyTTL > 0, "Invalid config: keyTTL = " + keyTTL);
			Assert.isTrue(updateTTLScheduleTime > 0, "Invalid config updateTTLScheduleTime = " + updateTTLScheduleTime);
			Assert.isTrue(workerCorePoolSize > 0, "Invalid config workerCorePoolSize = " + workerCorePoolSize);
			Assert.isTrue(workerMaxPoolSize > 0, "Invalid config workerMaxPoolSize = " + workerMaxPoolSize);
			Assert.isTrue(workerCorePoolSize <= workerMaxPoolSize,
				"Invalid config workerCorePoolSize = " + workerCorePoolSize + ", workerMaxPoolSize = " + workerMaxPoolSize);
			Assert.isTrue(workerThreadAliveTime > 0, "Invalid config workerThreadAliveTime = " + workerThreadAliveTime);
			Assert.isTrue(workerQueueSize > 0, "Invalid config workerQueueSize = " + workerQueueSize);
			Assert.isTrue(schedulerCorePoolSize > 0, "Invalid config schedulerCorePoolSize = " + schedulerCorePoolSize);
			return new JedisLockConfigs(this);
		}

	}

}
