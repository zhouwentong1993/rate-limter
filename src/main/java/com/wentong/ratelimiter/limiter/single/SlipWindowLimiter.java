package com.wentong.ratelimiter.limiter.single;

import com.wentong.ratelimiter.limiter.RateLimiter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class SlipWindowLimiter implements RateLimiter {

    private final int bucketCount;
    private final AtomicReferenceArray<Bucket> buckets;
    private final int bucketPerMs;
    private final int limitCountPerBucket;

    public SlipWindowLimiter(int limitCountPerSeconds, int bucketCount) {
        if (limitCountPerSeconds % bucketCount != 0 || 1000 % bucketCount != 0) {
            throw new IllegalArgumentException("Notice limitCountPerSeconds % bucketCount must be zero!");
        }
        this.bucketCount = bucketCount;
        this.buckets = new AtomicReferenceArray<>(bucketCount);
        this.bucketPerMs = 1000 / bucketCount;
        this.limitCountPerBucket = limitCountPerSeconds / bucketCount;
    }

    /**
     * 注意：该线程非并发安全，会有一定的 overflow。比如限流 1w，可能会超过 1w，但不会超过很多。
     * 如果改造成线程安全的，需要增加开销。对共享变量加同步操作。
     */
    @Override
    public boolean acquire() {
        long now = System.currentTimeMillis();
        int index = locIndex(now);
        Bucket bucket = buckets.get(index);
        // 该 bucket 已经过期了，需要更新 bucket
        if (bucket.startTimestamp + bucketPerMs < now) {
            bucket = new Bucket();
            bucket.startTimestamp = now - now % 1000 + (long) bucketPerMs * index;
            bucket.currentCount = new AtomicInteger(0);
            buckets.set(index, bucket);
            return true;
        } else {
            int currentCount = bucket.currentCount.intValue();
            if (currentCount < limitCountPerBucket) {
                return bucket.currentCount.compareAndSet(currentCount, currentCount + 1);
            } else {
                return false;
            }
        }
    }

    private int locIndex(long now) {
        int loc = (int) (now % 1000);
        for (int i = bucketCount - 1; i >= 0; i--) {
            if (bucketPerMs * i < loc) {
                Bucket bucket = buckets.get(i);
                if (bucket == null) {
                    bucket = new Bucket();
                    bucket.startTimestamp = now - loc + (long) bucketPerMs * i;
                    bucket.currentCount = new AtomicInteger(0);
                    buckets.set(i, bucket);
                }
                return i;
            }
        }
        throw new IllegalStateException();
    }

    static class Bucket {
        volatile long startTimestamp;
        volatile AtomicInteger currentCount;
    }

}
