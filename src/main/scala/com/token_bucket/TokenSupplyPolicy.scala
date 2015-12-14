package com.token_bucket

/**
  * Created on 15/12/5.
  * Author: ylgrgyq
  */
trait TokenSupplyPolicy {
  def tankNotFull(now: Long)

  def tankFull(now: Long)

  def supplyToken(now: Long): Int
}
