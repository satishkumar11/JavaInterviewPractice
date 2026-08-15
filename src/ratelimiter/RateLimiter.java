package ratelimiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

// RATE LIMITER — in-memory, per-endpoint config, Factory pattern to build the
// right algorithm from heterogeneous config, Strategy pattern (the Limiter
// interface) so each algorithm manages its own per-client state independently.
// Faithful implementation of: https://www.hellointerview.com/learn/low-level-design/problem-breakdowns/rate-limiter

// Immutable decision returned by every allow() call.
class RateLimitResult {
    private final boolean allowed;
    private final int remaining;
    private final Long retryAfterMs; // null when allowed - no need to retry

    public RateLimitResult(boolean allowed, int remaining, Long retryAfterMs) {
        this.allowed = allowed;
        this.remaining = remaining;
        this.retryAfterMs = retryAfterMs;
    }

    public boolean isAllowed() { return allowed; }
    public int getRemaining() { return remaining; }
    public Long getRetryAfterMs() { return retryAfterMs; }
}

// Strategy interface every rate limiting algorithm implements.
interface Limiter {
    RateLimitResult allow(String key);
}

// Per-client Token Bucket state: how many tokens remain, and when we last refilled.
class TokenBucket {
    double tokens;
    long lastRefillTime;

    TokenBucket(double initialTokens, long time) { this.tokens = initialTokens; this.lastRefillTime = time; }
}

// Bursts allowed up to `capacity`, refilling continuously at `refillRatePerSecond`.
class TokenBucketLimiter implements Limiter {
    private final int capacity;
    private final int refillRatePerSecond;
    private final Map<String, TokenBucket> buckets;

    public TokenBucketLimiter(int capacity, int refillRatePerSecond) {
        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.buckets = new HashMap<>();
    }

    public RateLimitResult allow(String key) {
        TokenBucket bucket = getOrCreateBucket(key);

        long now = System.currentTimeMillis();
        long elapsed = now - bucket.lastRefillTime;
        double tokensToAdd = (elapsed * refillRatePerSecond) / 1000.0;
        bucket.tokens = Math.min(capacity, bucket.tokens + tokensToAdd);
        bucket.lastRefillTime = now;

        if (bucket.tokens >= 1) {
            bucket.tokens -= 1;
            return new RateLimitResult(true, (int) Math.floor(bucket.tokens), null);
        } else {
            double tokensNeeded = 1 - bucket.tokens;
            long retryAfterMs = (long) Math.ceil((tokensNeeded * 1000) / refillRatePerSecond);
            return new RateLimitResult(false, 0, retryAfterMs);
        }
    }

    // Buckets are created lazily, full, on a client's first request - no
    // background refill thread needed, we just backfill elapsed time on demand.
    private TokenBucket getOrCreateBucket(String key) {
        if (!buckets.containsKey(key)) {
            buckets.put(key, new TokenBucket(capacity, System.currentTimeMillis()));
        }
        return buckets.get(key);
    }
}

// Per-client request history: a FIFO queue of timestamps within the window.
class RequestLog {
    Queue<Long> timestamps;

    RequestLog(Queue<Long> queue) { this.timestamps = queue; }
}

// Perfectly accurate but memory-heavy: tracks every request's exact timestamp
// in a rolling window instead of an aggregate count.
class SlidingWindowLogLimiter implements Limiter {
    private final int maxRequests;
    private final long windowMs;
    private final Map<String, RequestLog> logs;

    public SlidingWindowLogLimiter(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.logs = new HashMap<>();
    }

    public RateLimitResult allow(String key) {
        RequestLog log = getOrCreateLog(key);

        long now = System.currentTimeMillis();
        long cutoff = now - windowMs;

        while (!log.timestamps.isEmpty() && log.timestamps.peek() < cutoff) {
            log.timestamps.poll();
        }

        if (log.timestamps.size() < maxRequests) {
            log.timestamps.add(now);
            return new RateLimitResult(true, maxRequests - log.timestamps.size(), null);
        } else {
            long oldestTimestamp = log.timestamps.peek();
            long retryAfterMs = (oldestTimestamp + windowMs) - now;
            return new RateLimitResult(false, 0, retryAfterMs);
        }
    }

    private RequestLog getOrCreateLog(String key) {
        if (!logs.containsKey(key)) {
            logs.put(key, new RequestLog(new LinkedList<>()));
        }
        return logs.get(key);
    }
}

// Builds the right Limiter from raw config data - centralizes the "which
// algorithm class" decision instead of scattering it across the codebase.
class LimiterFactory {
    @SuppressWarnings("unchecked")
    public Limiter create(Map<String, Object> externalConfig) {
        String algorithm = (String) externalConfig.get("algorithm");
        Map<String, Object> algoConfig = (Map<String, Object>) externalConfig.get("algoConfig");

        switch (algorithm) {
            case "TokenBucket":
                return new TokenBucketLimiter(
                        ((Number) algoConfig.get("capacity")).intValue(),
                        ((Number) algoConfig.get("refillRatePerSecond")).intValue()
                );
            case "SlidingWindowLog":
                return new SlidingWindowLogLimiter(
                        ((Number) algoConfig.get("maxRequests")).intValue(),
                        ((Number) algoConfig.get("windowMs")).longValue()
                );
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }
    }
}

// Orchestrator: one Limiter per configured endpoint, falling back to a
// default for anything unconfigured. Everything is built eagerly at startup.
public class RateLimiter {
    private final Map<String, Limiter> limiters;
    private final Limiter defaultLimiter;

    public RateLimiter(List<Map<String, Object>> configs, Map<String, Object> defaultConfig) {
        this.limiters = new HashMap<>();
        LimiterFactory factory = new LimiterFactory();

        for (Map<String, Object> config : configs) {
            String endpoint = (String) config.get("endpoint");
            if (endpoint == null) {
                continue;
            }
            Limiter limiter = factory.create(config);
            limiters.put(endpoint, limiter);
        }

        this.defaultLimiter = factory.create(defaultConfig);
    }

    public RateLimitResult allow(String clientId, String endpoint) {
        Limiter limiter = limiters.getOrDefault(endpoint, defaultLimiter);
        return limiter.allow(clientId);
    }

    public static void main(String[] args) throws InterruptedException {
        Map<String, Object> searchAlgoConfig = new HashMap<>();
        searchAlgoConfig.put("capacity", 5);
        searchAlgoConfig.put("refillRatePerSecond", 5);
        Map<String, Object> searchConfig = new HashMap<>();
        searchConfig.put("endpoint", "/search");
        searchConfig.put("algorithm", "TokenBucket");
        searchConfig.put("algoConfig", searchAlgoConfig);

        Map<String, Object> uploadAlgoConfig = new HashMap<>();
        uploadAlgoConfig.put("maxRequests", 3);
        uploadAlgoConfig.put("windowMs", 800L);
        Map<String, Object> uploadConfig = new HashMap<>();
        uploadConfig.put("endpoint", "/upload");
        uploadConfig.put("algorithm", "SlidingWindowLog");
        uploadConfig.put("algoConfig", uploadAlgoConfig);

        Map<String, Object> defaultAlgoConfig = new HashMap<>();
        defaultAlgoConfig.put("capacity", 2);
        defaultAlgoConfig.put("refillRatePerSecond", 1);
        Map<String, Object> defaultConfig = new HashMap<>();
        defaultConfig.put("algorithm", "TokenBucket");
        defaultConfig.put("algoConfig", defaultAlgoConfig);

        RateLimiter rateLimiter = new RateLimiter(new ArrayList<>(List.of(searchConfig, uploadConfig)), defaultConfig);

        System.out.println("--- Token Bucket on /search (capacity=5, refill=5/s) ---");
        for (int i = 0; i < 7; i++) {
            RateLimitResult r = rateLimiter.allow("alice", "/search");
            System.out.println("request " + i + ": allowed=" + r.isAllowed() + " remaining=" + r.getRemaining() + " retryAfterMs=" + r.getRetryAfterMs());
        }
        RateLimitResult denied = rateLimiter.allow("alice", "/search");
        System.out.println("bucket drained: allowed=" + denied.isAllowed() + " retryAfterMs=" + denied.getRetryAfterMs());
        Thread.sleep(denied.getRetryAfterMs() + 20);
        RateLimitResult afterWait = rateLimiter.allow("alice", "/search");
        System.out.println("after waiting: allowed=" + afterWait.isAllowed() + " remaining=" + afterWait.getRemaining());

        System.out.println("\n--- Sliding Window Log on /upload (max=3, window=800ms) ---");
        for (int i = 0; i < 3; i++) {
            RateLimitResult r = rateLimiter.allow("bob", "/upload");
            System.out.println("request " + i + ": allowed=" + r.isAllowed() + " remaining=" + r.getRemaining());
        }
        RateLimitResult uploadDenied = rateLimiter.allow("bob", "/upload");
        System.out.println("4th immediate request: allowed=" + uploadDenied.isAllowed() + " retryAfterMs=" + uploadDenied.getRetryAfterMs());
        Thread.sleep(uploadDenied.getRetryAfterMs() + 20);
        RateLimitResult uploadAfterWait = rateLimiter.allow("bob", "/upload");
        System.out.println("after window slides: allowed=" + uploadAfterWait.isAllowed());

        System.out.println("\n--- Default limiter for unconfigured endpoint ---");
        System.out.println("call 1: " + rateLimiter.allow("carol", "/unknown").isAllowed());
        System.out.println("call 2: " + rateLimiter.allow("carol", "/unknown").isAllowed());
        System.out.println("call 3 (should be denied, capacity=2): " + rateLimiter.allow("carol", "/unknown").isAllowed());

        System.out.println("\n--- Unknown algorithm rejection ---");
        try {
            Map<String, Object> badConfig = new HashMap<>();
            badConfig.put("algorithm", "LeakyBucket");
            badConfig.put("algoConfig", new HashMap<>());
            new LimiterFactory().create(badConfig);
        } catch (IllegalArgumentException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
