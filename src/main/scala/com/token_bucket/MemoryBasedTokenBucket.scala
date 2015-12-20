package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class MemoryBasedTokenBucket(capacity: Int, interval: Long, minInterval: Long = 0, unit: TimeUnit = TimeUnit.SECONDS, payForFailedTry: Boolean = true) extends TokenBucket {
  require(capacity > 0, "Bucket Capacity should bigger than 0")
  require(interval > 0, "Interval time should bigger than 0")
  require(minInterval >= 0, "Minimum interval time should not negative")
  require(unit != null, "Please give me time unit for parameter minInterval and interval")

  private val tokenSupplyPolicy = new FixedRateTokenSupplyPolicy(Math.ceil(capacity.toDouble / interval).toLong, 1, unit)

  private var tokensCount = capacity
  private var lastConsumeTime = 0L
  private val minIntervalInNanos = unit.toNanos(minInterval)

  private def refill(now: Long, tokensSupplied: Int) = {
    tokensCount = math.min(capacity, tokensSupplied + tokensCount)
    if (tokensCount == capacity) {
      tokenSupplyPolicy.tankFull(now)
    }
  }

  override def tryConsume(tokenInNeed: Int) = this.synchronized {
    require(tokenInNeed > 0)

    val now = System.nanoTime()

    if (minIntervalInNanos != 0 && now - lastConsumeTime < minIntervalInNanos) {
      false
    } else {
      refill(now, tokenSupplyPolicy.supplyToken(now))

      if (tokensCount >= tokenInNeed) {
        tokensCount -= tokenInNeed
        tokenSupplyPolicy.tankNotFull(now)
        lastConsumeTime = now
        true
      } else {
        if (payForFailedTry) tokensCount -= tokenInNeed
        false
      }
    }
  }
}
