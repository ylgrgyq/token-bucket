package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class MemBasedTokenBucket(capacity: Int, interval: Long, unit: TimeUnit, minInterval: Long, payForFailedTry: Boolean) extends TokenBucket {
  require(capacity > 0, "Bucket Capacity should bigger than 0")
  require(interval > 0, "Interval time should bigger than 0")
  require(minInterval >= 0, "Minimum interval time should not negative")
  require(unit != null, "Please give me time unit for parameter minInterval and interval")

  private val tokenSupplyPolicy = new FixedRateTokenSupplyPolicy(Math.ceil(capacity.toDouble / interval).toLong, 1, unit)

  private var tokensCount = capacity
  private var lastConsumeTime = 0L
  private var lastFillTime = 0L
  private val minIntervalInNanos = unit.toNanos(minInterval)

  private def refill(now: Long, tokensSupplied: Int) = {
    tokensCount = math.min(capacity, tokensSupplied + tokensCount)

    if (tokensCount == capacity) lastFillTime = 0
  }

  override def tryConsume(tokenInNeed: Int = 1) = this.synchronized {
    require(tokenInNeed > 0)

    val now = System.nanoTime()

    if (minIntervalInNanos != 0 && now - lastConsumeTime < minIntervalInNanos) {
      false
    } else {
      val (suppliedToken, newFillTime) = tokenSupplyPolicy.supplyToken(now, lastFillTime)
      lastFillTime = newFillTime
      refill(now, suppliedToken)

      if (tokensCount >= tokenInNeed) {
        tokensCount -= tokenInNeed
        if (lastFillTime == 0) lastFillTime = now
        lastConsumeTime = now
        true
      } else {
        if (payForFailedTry) tokensCount -= tokenInNeed
        false
      }
    }
  }

  override def isBucketFull(): Boolean = this.synchronized(tokensCount == capacity)
}

case class MemBasedTokenBucketBuilder {
  var capacity = _
  var interval = _
  var minInterval = 0L
  var payForFailedTry = true

  def capacity(c: Int) = {
    require(c > 0, "Bucket Capacity should bigger than 0")

    capacity = c
    this
  }

  def interval(i: Long, u: TimeUnit = TimeUnit.SECONDS) = {
    require(i > 0, "Interval time should bigger than 0")
    require(u != null, "Interval time unit should not be null")

    interval = u.toNanos(i)
    this
  }

  def minInterval(min: Long, u: TimeUnit = TimeUnit.MILLISECONDS) = {
    require(min >= 0, "Minimum interval time should not negative")
    require(u != null, "Minimum interval time unit should not be null")

    minInterval = u.toNanos(min)
    this
  }

  def payForFailedTry(p: Boolean) = {
    payForFailedTry = p
    this
  }

  def build(): MemBasedTokenBucket = new MemBasedTokenBucket(capacity, interval, TimeUnit.NANOSECONDS, minInterval, payForFailedTry)
}
