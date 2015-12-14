package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
class FixedRateTokenSupplyPolicy(tokensPerPeriod: Int, period: Int, unit: TimeUnit) extends TokenSupplyPolicy {
  require(tokensPerPeriod >= 0, "Tokens supplied per period should not negative")
  require(period > 0, "Token supply period should not negative")
  require(unit != null)

  val periodInNano = unit.toNanos(period)
  var lastFillTime: Long = 0

  def tankNotFull(now: Long) = {
    require(now > 0, "now should not be negative")

    lastFillTime = now
  }

  def tankFull(now: Long) = {
    require(now > 0, "now should not be negative")

    lastFillTime = 0
  }

  def supplyToken(now: Long): Int = {
    require(now > 0, "now should not be negative")

    if (lastFillTime == 0) {
      0
    } else {
      val periods = math.min(0, ((now - lastFillTime) / periodInNano).toInt)

      val tokensSupplied = periods * tokensPerPeriod
      lastFillTime += periods * period

      tokensSupplied
    }
  }
}
