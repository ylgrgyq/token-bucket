package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class MemoryBasedTokenBucket(capacity: Int, interval: Long, minInterval: Long = 0, unit: TimeUnit = TimeUnit.SECONDS) extends TokenBucket {
  require(capacity > 0, "Bucket Capacity should bigger than 0")
  require(minInterval >= 0, "Minimum interval time should not negative")
  require(interval >= 0, "Interval should not negative")

  private val tokenSupplyPolicy = new FixedRateTokenSupplyPolicy(Math.ceil(capacity.toDouble / interval).toLong, 1, unit)

  private var tokensCount = capacity
  private var lastConsumeTime = 0L

  private def refill(now: Long, tokensSupplied: Int) = {
    tokensCount = math.min(capacity, tokensSupplied + tokensCount)
    if (tokensCount == capacity) {
      tokenSupplyPolicy.tankFull(now)
    }
  }

  override def tryConsume(tokenInNeed: Int) = this.synchronized {
    require(tokenInNeed > 0)

    val now = System.nanoTime()
    if (minInterval != 0 && now - lastConsumeTime < minInterval) {
      false
    } else {
      refill(now, tokenSupplyPolicy.supplyToken(now))

      tokensCount -= tokenInNeed
      if (tokensCount >= 0) {
        tokenSupplyPolicy.tankNotFull(now)
        lastConsumeTime = now
        true
      } else false
    }
  }
}
