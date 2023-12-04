package com.github.example.spring.jedis.lock.test;

import com.github.example.spring.jedis.lock.JedisLockManager;
import com.github.example.spring.jedis.lock.annotation.DistributedLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringIntegrationTest {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringIntegrationTest.class, args);

        TestFunctions testFunctions = context.getBean(TestFunctions.class);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(() -> testFunctions.fooA());
        executorService.execute(() -> testFunctions.fooA());
        executorService.execute(() -> System.out.println(String.format("response:%s", testFunctions.fooD("JedisLock"))));
    }

}

@ComponentScan("com.github.example.spring.jedis.lock")
@Configuration
class TestConfiguration {
    @Bean
    public JedisPool jedisPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(20);
        config.setMaxIdle(50);
        config.setMaxTotal(100);
        config.setMaxWaitMillis(2000);

        return new JedisPool(config, "127.0.0.1", 6379);
    }

    @Bean
    public JedisLockManager jedisLockManager() {
        return new JedisLockManager(jedisPool());
    }
}

@Component
class TestFunctions {
    @DistributedLock(keyFormat = "test1")
    public void fooA() {
        fooB();
    }

    @DistributedLock(keyFormat = "test1")
    public void fooB() {
        fooC();
    }

    @DistributedLock(keyFormat = "test1")
    public void fooC() {
        try {
            // Do something
            System.out.println(String.format("Thread: %s acquire lock", Thread.currentThread().getName()));
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
        }
    }

    @DistributedLock(keyType = DistributedLock.KeyType.ARGUMENTS, keyFormat = "test2|%s")
    public String fooD(String str) {
        try {
            // Do something
            System.out.println("request: " + str);
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
        }
        return str;
    }
}
