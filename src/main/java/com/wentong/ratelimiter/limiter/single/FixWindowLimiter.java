package com.wentong.ratelimiter.limiter.single;

import com.wentong.ratelimiter.limiter.RateLimiter;

import java.util.concurrent.atomic.AtomicInteger;

public class FixWindowLimiter implements RateLimiter {

    private final int limitCountPerSeconds;
    private AtomicInteger currentCount;
    private volatile long lastTimestamp;

    public FixWindowLimiter(int limitCountPerSeconds) {
        this.limitCountPerSeconds = limitCountPerSeconds;
        this.lastTimestamp = System.currentTimeMillis();
        this.currentCount = new AtomicInteger(0);
    }

    @Override
    public boolean acquire() {
        long now = System.currentTimeMillis();
        if (now - lastTimestamp < 1000) {
            if (currentCount.intValue() < limitCountPerSeconds) {
                currentCount.incrementAndGet();
                return true;
            } else {
                return false;
            }
        } else {
            lastTimestamp = now;
            currentCount = new AtomicInteger(1);
            return true;
        }
    }

}
