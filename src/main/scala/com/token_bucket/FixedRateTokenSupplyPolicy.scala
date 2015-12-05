package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
class FixedRateTokenSupplyPolicy(capacity: Int, fillRate: Int, unit: TimeUnit) extends TokenSupplyPolicy {

  var fillStartTime: Long = 0

  def tankNotFull(bucket: TokenBucket): Unit = {
    fillStartTime = System.nanoTime()
  }

  def supplyToken(bucket: TokenBucket): Int = {
    val current = System.nanoTime()

    val tokenNeedToFill = ((current - fillStartTime) / unit.toNanos(1)).toInt * fillRate

    fillStartTime = current

    tokenNeedToFill
  }
}
