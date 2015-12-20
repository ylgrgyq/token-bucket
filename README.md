# Introduction

This is a token-bucket implementation either in-memory or in-redis for rate limiting.

It uses [killme2008/clj-rate-limiter](https://github.com/killme2008/clj-rate-limiter) and [bbeck/token-bucket](https://github.com/bbeck/token-bucket) as reference and does some enhancement.

# Usage


# Basic

## In-memory bucket

Create in-memory token bucket to limit request rate within 30 requests/seconds:
```scala
val tokenBuckets = new GroupedMemBasedTokenBucket("default", 30, 1, unit = TimeUnit.SECONDS) // "default" is namespace to distinguish from each GroupedRedisBasedTokenBucket instance

for (i <- 0 to 30)
   assert(tokenBuckets.tryConsume("key1"))
   assert(tokenBuckets.tryConsume("key2"))

assert(! tokenBuckets.tryConsume("key1"))
assert(! tokenBuckets.tryConsume("key2"))
```

If you want to manage namespace and id for yourself, you can use MemBasedTokenBucket instead. All the parameter is the same with GroupedMemBasedTokenBucket except that MemBasedTokenBucket doesn't have namespace and id parameter.
```scala
val tokenBuckets = new MemBasedTokenBucket(30, 1, unit = TimeUnit.SECONDS) 

for (i <- 0 to 30)
   assert(tokenBuckets.tryConsume())

assert(! tokenBuckets.tryConsume())
```

## Redis bucket

Create redis based token bucket to limit request rate within 30 requests/seconds:
```
val tokenBuckets = new GroupedRedisBasedTokenBucket("default", 30, 1, unit = TimeUnit.SECONDS, redis = new RedisClient()) // "default" is namespace to distinguish from each GroupedRedisBasedTokenBucket instance

for (i <- 0 to 30)
   assert(tokenBuckets.tryConsume("key1"))
   assert(tokenBuckets.tryConsume("key2"))

assert(! tokenBuckets.tryConsume("key1"))
assert(! tokenBuckets.tryConsume("key2"))
```

For RedisClient, please checkout [etaty/rediscala](https://github.com/etaty/rediscala).

# License

Copyright © 2015 ylgrgyq

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
