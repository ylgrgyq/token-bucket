package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait TokenBucket {

  def tryConsume(): Boolean = tryConsume(1)

  def tryConsume(tokenInNeed: Int): Boolean

  def isBucketFull(): Boolean
}

trait GropuedTokenBucket {
  def tryConsume(id: String, tokenInNeed: Int): Boolean

  def tryConsume(id: String): Boolean = tryConsume(id, 1)

  def isBucketFull(id: String): Boolean
}

