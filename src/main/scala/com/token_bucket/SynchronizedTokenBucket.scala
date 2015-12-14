package com.token_bucket

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class SynchronizedTokenBucket(capacity: Int, minInterval: Long, tokenSupplyPolicy: TokenSupplyPolicy) extends TokenBucket {
  require(capacity > 0, "Bucket Capacity should bigger than 0")
  require(minInterval >= 0, "Minimum interval time should not negative")
  require(tokenSupplyPolicy != null, "Please give me a token supply policy")

  private var tokensCount = capacity
  private var lastConsumeTime = 0L

  private def refill(now: Long, tokensSupplied: Int) = {
    tokensCount = math.min(capacity, tokensSupplied + tokensCount)
    if (tokensCount == capacity){
      tokenSupplyPolicy.tankFull(now)
    }
  }

  override def tryConsume(tokenInNeed: Int) = this.synchronized {
    require(tokenInNeed > 0)

    val now = System.nanoTime()
    if (now - lastConsumeTime < minInterval){
      false
    } else {
      refill(now, tokenSupplyPolicy.supplyToken(now))

      if (tokensCount >= tokenInNeed) {
        tokensCount -= tokenInNeed
        tokenSupplyPolicy.tankNotFull(now)
        lastConsumeTime = now
        true
      } else false
    }
  }
}
