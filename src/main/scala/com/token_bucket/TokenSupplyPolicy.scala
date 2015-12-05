package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait TokenSupplyPolicy {
  def tankNotFull(bucket: TokenBucket)

  def supplyToken(bucket: TokenBucket): Int
}
