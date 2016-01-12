package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait TokenSupplyPolicy {
  def supplyToken(now: Long, lastFillTime: Long): (Int, Long)
}
