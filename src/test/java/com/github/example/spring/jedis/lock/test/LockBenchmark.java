package com.github.example.spring.jedis.lock.test;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class LockBenchmark {
    protected static final int THREAD_SIZE = 10;
    protected static final int NUMBER_OF_TASK = 1000000;
    protected static final int TASK_TIME = 3;

    protected static DecimalFormat NUMBER_FORMAT;

    static {
        NUMBER_FORMAT = new DecimalFormat("0.00");
        NUMBER_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
    }

    protected void benchmark(Runnable task) {
        CountDownLatch latch = new CountDownLatch(THREAD_SIZE);
        long begin = System.currentTimeMillis();
        for (int i = 0; i < THREAD_SIZE; i++) {
            new Thread(() -> {
                for (int j = 0; j < NUMBER_OF_TASK / THREAD_SIZE; j++) {
                    task.run();
                }
                latch.countDown();
            }).start();
        }
        try {
            latch.await();
            /*
             * Benchmark result
             * */
            long end = System.currentTimeMillis();
            long rt = TimeUnit.MILLISECONDS.toSeconds(end - begin);

            String tps = NUMBER_FORMAT.format((double) NUMBER_OF_TASK / rt);
            System.out.println(String.format("[ThreadSize] %s - [NumberOfTask] %s - [Runtime] %ss, [TPS] %s",
                    THREAD_SIZE, NUMBER_OF_TASK, rt, tps));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
