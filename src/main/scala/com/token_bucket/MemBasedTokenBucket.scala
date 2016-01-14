package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class MemBasedTokenBucket(builder: MemBasedTokenBucket.MemBasedTokenBucketBuilder) extends TokenBucket {
  private val capacity = builder._capacity
  private val minInterval = builder._minInterval
  private val payForFailedTry = builder._payForFailedTry
  private val tokenSupplyPolicy = new FixedRateTokenSupplyPolicy(builder._tokensPerPeriod, builder._period, TimeUnit.NANOSECONDS)

  private var _tokensCount = capacity
  private var lastConsumeTime = 0L
  private var lastFillTime = 0L

  override def tryConsume(tokenInNeed: Int = 1) = this.synchronized {
    require(tokenInNeed > 0)

    val now = System.nanoTime()

    if (minInterval != 0 && now - lastConsumeTime < minInterval) {
      false
    } else {
      val (suppliedToken, newFillTime) = tokenSupplyPolicy.supplyToken(now, lastFillTime)
      _tokensCount = math.min(capacity, suppliedToken + _tokensCount)
      lastFillTime = if (_tokensCount == capacity) 0L else newFillTime

      if (_tokensCount >= tokenInNeed) {
        _tokensCount -= tokenInNeed
        if (lastFillTime == 0) lastFillTime = now
        lastConsumeTime = now
        true
      } else {
        if (payForFailedTry) _tokensCount -= tokenInNeed
        false
      }
    }
  }

  def tokensCount() = _tokensCount

  override def isBucketFull(): Boolean = this.synchronized(_tokensCount == capacity)
}

object MemBasedTokenBucket {

  case class MemBasedTokenBucketBuilder() {
    private[MemBasedTokenBucket] var _capacity: Int = _
    private[MemBasedTokenBucket] var _period: Long = _
    private[MemBasedTokenBucket] var _minInterval = 0L
    private[MemBasedTokenBucket] var _payForFailedTry = true
    private[MemBasedTokenBucket] var _tokensPerPeriod = 1

    def capacity(c: Int) = {
      require(c > 0, "Bucket Capacity should bigger than 0")

      _capacity = c
      this
    }

    def interval(i: Long, u: TimeUnit = TimeUnit.SECONDS) = {
      require(i > 0, "Interval time should bigger than 0")
      require(u != null, "Interval time unit should not be null")

      _period = u.toNanos(i)
      this
    }

    def minInterval(min: Long, u: TimeUnit = TimeUnit.MILLISECONDS) = {
      require(min >= 0, "Minimum interval time should not negative")
      require(u != null, "Minimum interval time unit should not be null")

      _minInterval = u.toNanos(min)
      this
    }

    def payForFailedTry(p: Boolean) = {
      _payForFailedTry = p
      this
    }

    def tokensPerInterval(t: Int) = {
      _tokensPerPeriod = t
      this
    }

    def build(): MemBasedTokenBucket = new MemBasedTokenBucket(this)
  }
}
