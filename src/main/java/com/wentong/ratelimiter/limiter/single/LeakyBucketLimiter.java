package com.wentong.ratelimiter.limiter.single;

import com.wentong.ratelimiter.limiter.RateLimiter;

public class LeakyBucketLimiter implements RateLimiter {

    @Override
    public boolean acquire() {
        return false;
    }

}
