package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
class FixedRateTokenSupplyPolicy(tokensPerPeriod: Int, period: Long, unit: TimeUnit) extends TokenSupplyPolicy {
  require(tokensPerPeriod >= 0, "Tokens supplied per period should not negative")
  require(period > 0, "Token supply period should not negative")
  require(unit != null)

  val periodInNano = unit.toNanos(period)

  println(s"tokens per period: $tokensPerPeriod period: $period")

  def supplyToken(now: Long, lastFillTime: Long): (Int, Long) = {
    require(now > 0, "now should not be negative")

    if (lastFillTime == 0) {
      (0, lastFillTime)
    } else {
      val periods = math.max(0, ((now - lastFillTime) / periodInNano).toInt)

      val tokensSupplied = periods * tokensPerPeriod

      (tokensSupplied, lastFillTime + periods * periodInNano)
    }
  }
}
