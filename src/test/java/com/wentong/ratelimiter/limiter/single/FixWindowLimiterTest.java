package com.wentong.ratelimiter.limiter.single;

import com.wentong.ratelimiter.limiter.RateLimiter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class FixWindowLimiterTest {

    @Test
    void acquire() {
        RateLimiter limiter = new FixWindowLimiter(10);
        for (int i = 0; i < 10; i++) {
            assertTrue(limiter.acquire());
        }
        for (int i = 0; i < 10; i++) {
            assertFalse(limiter.acquire());
        }
    }

    // 150 个线程，每个线程 100 个请求。通过 10000 个，没通过 5000 个。
    @Test
    void acquireWhenMultiThread() throws Exception {
        int threadCount = 150;
        int runPerThread = 100;
        AtomicInteger acquireCount = new AtomicInteger(0);
        AtomicInteger unAcquireCount = new AtomicInteger(0);

        RateLimiter limiter = new FixWindowLimiter(10000);
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