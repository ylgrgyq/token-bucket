package com.token_bucket

import java.util.concurrent.TimeUnit

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class TokenBucket(initToken: Int, capacity: Int, fillRate: Int, unit: TimeUnit) {

  var tokenInBucket = initToken
  var fillStartTime: Long = if (initToken < capacity) System.nanoTime() else 0

  def this (maxToken: Int, fillRate: Int) =
    this(maxToken, maxToken, fillRate, TimeUnit.SECONDS)

  def tryGet(tokenInNeed: Int) = {
    val current = System.nanoTime()

    val isFilling = tokenInBucket < capacity
    if (isFilling) {
      val tokenNeedToFill = ((current - fillStartTime) / unit.toNanos(1)).toInt * fillRate
      tokenInBucket = if (tokenInBucket + tokenNeedToFill > capacity) capacity else tokenInBucket + tokenNeedToFill
      fillStartTime = current
    }

    if (tokenInBucket >= tokenInNeed) {
      tokenInBucket -= tokenInNeed

      if (!isFilling) {
        fillStartTime = current
      }
      true
    } else false
  }
}
