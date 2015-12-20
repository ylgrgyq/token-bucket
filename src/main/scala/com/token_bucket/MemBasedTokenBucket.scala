package com.token_bucket

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import akka.actor.{Cancellable, Scheduler}

import scala.concurrent.duration.FiniteDuration
import scala.collection.JavaConverters._

/**
  * Created on 15/12/3.
  * Author: ylgrgyq
  */

class GroupedMemBasedTokenBucket(namespace: String, capacity: Int, interval: Long, unit: TimeUnit = TimeUnit.SECONDS, minInterval: Long = 0, payForFailedTry: Boolean = true)
                                (implicit scheduler: Scheduler) extends GropuedTokenBucket {
  require(capacity > 0, "Bucket Capacity should bigger than 0")
  require(interval > 0, "Interval time should bigger than 0")
  require(minInterval >= 0, "Minimum interval time should not negative")
  require(unit != null, "Please give me time unit for parameter minInterval and interval")

  private val buckets = new ConcurrentHashMap[String, MemBasedTokenBucket]().asScala
  private val timeouts = new ConcurrentHashMap[String, Cancellable]().asScala

  def key(id: String) = s"$namespace-$id"

  def setTimeout(k:String) ={
    timeouts(k) = scheduler.scheduleOnce(FiniteDuration(interval, unit)){
      buckets.remove(k)
      timeouts.remove(k)
    }
  }

  override def tryConsume(id: String, tokenInNeed: Int): Boolean = this.synchronized {
    require(tokenInNeed > 0)

    val k = key(id)

    timeouts.get(k) match {
      case Some(cancelHandle) =>
        cancelHandle.cancel()
      case _ => _
    }
    setTimeout(k)

    buckets.get(k) match {
      case Some(bucket) => bucket.tryConsume(tokenInNeed)
      case _ =>
        val bucket = new MemBasedTokenBucket(capacity, interval, unit, minInterval, payForFailedTry)
        buckets(k) = bucket
        bucket.tryConsume(tokenInNeed)
    }
  }

  override def isBucketFull(id: String): Boolean = this.synchronized {
    buckets.get(key(id)) match {
      case Some(bucket) => bucket.isBucketFull()
      case _ => false
    }
  }
}

class MemBasedTokenBucket(capacity: Int, interval: Long, unit: TimeUnit = TimeUnit.SECONDS, minInterval: Long = 0, payForFailedTry: Boolean = true) extends TokenBucket {
  require(capacity > 0, "Bucket Capacity should bigger than 0")
  require(interval > 0, "Interval time should bigger than 0")
  require(minInterval >= 0, "Minimum interval time should not negative")
  require(unit != null, "Please give me time unit for parameter minInterval and interval")

  private val tokenSupplyPolicy = new FixedRateTokenSupplyPolicy(Math.ceil(capacity.toDouble / interval).toLong, 1, unit)

  private var tokensCount = capacity
  private var lastConsumeTime = 0L
  private val minIntervalInNanos = unit.toNanos(minInterval)

  private def refill(now: Long, tokensSupplied: Int) = {
    tokensCount = math.min(capacity, tokensSupplied + tokensCount)
    if (tokensCount == capacity) {
      tokenSupplyPolicy.tankFull(now)
    }
  }

  override def tryConsume(tokenInNeed: Int) = this.synchronized {
    require(tokenInNeed > 0)

    val now = System.nanoTime()

    if (minIntervalInNanos != 0 && now - lastConsumeTime < minIntervalInNanos) {
      false
    } else {
      refill(now, tokenSupplyPolicy.supplyToken(now))

      if (tokensCount >= tokenInNeed) {
        tokensCount -= tokenInNeed
        tokenSupplyPolicy.tankNotFull(now)
        lastConsumeTime = now
        true
      } else {
        if (payForFailedTry) tokensCount -= tokenInNeed
        false
      }
    }
  }

  override def isBucketFull(): Boolean = this.synchronized(tokensCount == capacity)
}
