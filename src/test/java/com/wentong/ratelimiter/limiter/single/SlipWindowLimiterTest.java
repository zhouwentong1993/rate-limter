package com.wentong.ratelimiter.limiter.single;

import com.wentong.ratelimiter.limiter.RateLimiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


class SlipWindowLimiterTest {

    @Test
    void acquire() {
        // 每秒最多 10 个请求，分为 5 个 bucket 存储。
        RateLimiter limiter = new SlipWindowLimiter(10, 5);
        for (int i = 0; i < 2; i++) {
            assertTrue(limiter.acquire());
        }
        assertFalse(limiter.acquire());
    }

    @Test
    void acquireWhenThreadSleep() throws Exception {
        RateLimiter limiter = new SlipWindowLimiter(10, 5);
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.acquire());
            TimeUnit.MILLISECONDS.sleep(150);
        }
    }

    @Test
    void acquireWhenMultiThreadWithOneBucket() throws Exception {
        RateLimiter limiter = new SlipWindowLimiter(10000, 1);

        int threadCount = 150;
        int runPerThread = 100;
        AtomicInteger acquireCount = new AtomicInteger(0);
        AtomicInteger unAcquireCount = new AtomicInteger(0);

        CountDownLatch startLatch = new CountDownLatch(threadCount);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                startLatch.countDown();
                for (int j = 0; j < runPerThread; j++) {
                    if (limiter.acquire()) {
                        acquireCount.incrementAndGet();
                    } else {
                        unAcquireCount.incrementAndGet();
                    }
                }
                endLatch.countDown();
            }, "Thread:" + i).start();
        }
        startLatch.await();
        endLatch.await();
        assertEquals(10000, acquireCount.intValue());
        assertEquals(5000, unAcquireCount.intValue());

    }
}