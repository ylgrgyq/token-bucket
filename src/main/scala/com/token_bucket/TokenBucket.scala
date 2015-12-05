package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait TokenBucket {

  def tryGet(tokenInNeed: Int)
}

object TokenBucket {

}
