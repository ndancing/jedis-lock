# jedis-lock

## Jedis Lock Config

```Java
JedisLockConfigs lockConfig = JedisLockConfigs.builder()
    .keyTTL(5000) // Lock key expiration time (mils). Default 30000
    .updateTTLScheduleTime(1000) // Watchdog schedule time (mils). Default 10000
    .multiLockWaitingTime(300) // Internal lock waiting time. Using for RedLock
    .build();

```

## JedisLockManager (Singleton)
If the used Redis configuration is Pool or JedisCluster, single-reentrant lock is used by default. If the input configuration is List of Pools, red-lock is used for locking.

- Single Reentrant Lock

For single Redis node (Single Pool)
```java
JedisPoolConfig poolConfig = new JedisPoolConfig();
config.setMinIdle(10);
config.setMaxIdle(50);
config.setMaxTotal(100);
config.setMaxWaitMillis(1000);
	
JedisLockManager lockManager = new JedisLockManager(new JedisPool(config, "127.0.0.1", 6379), lockConfig);
```

For cluster Redis (JedisCluster)
```java
JedisPoolConfig poolConfig = new JedisPoolConfig();
config.setMinIdle(10);
config.setMaxIdle(50);
config.setMaxTotal(100);
config.setMaxWaitMillis(1000);

JedisLockManager lockManager = new JedisLockManager(new JedisCluster(new HostAndPort("127.0.0.1", 6379), poolConfig), lockConfig);
```

- Red Lock

Requires number of redis nodes greater or equals 3 (should be odd number) and these nodes must be deployed independently, with no state between each node, and no master-slave copy or cluster management.
```Java
JedisPoolConfig poolConfig = new JedisPoolConfig();
	config.setMinIdle(10);
	config.setMaxIdle(50);
	config.setMaxTotal(100);
	config.setMaxWaitMillis(1000);

JedisLockManager lockManager = new JedisLockManager(Arrays.asList(
    new JedisPool(config, "127.0.0.1", 6379),
    new JedisPool(config, "127.0.0.1", 6380),
    new JedisPool(config, "127.0.0.1", 6381)), lockConfig);
```

## API Usage

- Lock Object
```Java
JedisLock lock = lockManager.getLock("lock_name");
```

- Operations
```Java
/*
 * Acquire the lock. If lock cannot be acquired, the thread will be blocked (Blocking lock)
 */

try {
    lock.lock();
} catch () {
    //...
} finally {
    lock.unlock(); //Release lock
}
```
```Java
/*
 * Try to acquire the lock, and return immediately if unable to acquire the lock (Non-blocking lock)
 */

try {
    lock.tryLock();
} catch () {
    //...
} finally {
    lock.unlock(); //Release lock
}
//
```
```Java
/*
 * Try to acquire lock with maximum waiting time
 */

try {
    lock.tryLock(1, TimeUnit.SECONDS);
} catch () {
    //...
} finally {
    lock.unlock(); //Release lock
}
```
```Java
/*
 * Force unlocking
 */

lock.forceUnlock();
```

## SpringBoot Integration

Beans Configuration

```Java
@ComponentScan("com.github.example.spring.jedis.lock")
@Configuration
class JedisLockConfiguration {

    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        config.setMinIdle(10);
        config.setMaxIdle(50);
        config.setMaxTotal(100);
        config.setMaxWaitMillis(1000);
        return new JedisPool(poolConfig, "127.0.0.1", 6379);
    }

    @Bean
    public JedisLockConfigs jedisLockConfigs() {
        return JedisLockConfigs.builder()
            .keyTTL(5000) // Lock key expiration time (mils). Default 30000
            .updateTTLScheduleTime(1000) // Watchdog schedule time (mils). Default 10000
            .multiLockWaitingTime(300) // Internal lock waiting time. Using for RedLock
            .build();
    }

    @Bean
    public JedisLockManager jedisLockManager() {
        JedisLockManager jedisLockManager = new JedisLockManager(jedisPool(), jedisLockConfigs());
        return jedisLockManager;
    }
}
```

Annotation Usage
```Java
@DistributedLock(name = "lockName", method = DistributedLock.Method.LOCK)
public void foo(Object... args) {}

@DistributedLock(name = "lockName", method = DistributedLock.Method.TRY_LOCK)
public void foo(Object... args) {}

@DistributedLock(name = "lockName", method = DistributedLock.Method.TRY_LOCK, waitTime = 1000L, timeUnit = TimeUnit.MILLISECONDS)
public void foo(Object... args) {}
```

- name: lock name
- method: LOCK / TRY_LOCK. (It will throw exception in TRY_LOCK case if lock cannot be acquired)
- waitTime: Try lock with waiting time
- timeUnit: Time unit of waitTime
