package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait  TokenBucket {

  def tryConsume(): Boolean = tryConsume(1)

  def tryConsume(tokenInNeed: Int): Boolean
}

