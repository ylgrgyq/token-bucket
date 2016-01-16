package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait TokenBucket {
  def tryConsume(tokenInNeed: Int = 1): Boolean

  def tokensCount(): Int

  def isBucketFull(): Boolean
}
