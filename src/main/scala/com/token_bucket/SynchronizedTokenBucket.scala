package com.token_bucket

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */
class SynchronizedTokenBucket(initToken: Int, capacity: Int, tokenSupplyPolicy: TokenSupplyPolicy) extends TokenBucket {
  private var tokenTank = initToken

  private def refill(tokensSupplied: Int) = {
    tokenTank = if (tokensSupplied + tokenTank >= capacity) capacity else tokensSupplied + tokenTank
  }

  def tryGet(tokenInNeed: Int) = {
    this.synchronized {
      refill(tokenSupplyPolicy.supplyToken(this))

      if (tokenTank >= tokenInNeed) {
        tokenTank -= tokenInNeed
        tokenSupplyPolicy.tankNotFull(this)
        true
      } else false
    }
  }
}
