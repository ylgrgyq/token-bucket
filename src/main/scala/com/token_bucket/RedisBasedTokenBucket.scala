package com.token_bucket

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import redis.RedisClient
import redis.api.Limit

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created on 15/12/14.
  * Author: ylgrgyq
  */

class RedisBasedTokenBucket(redis: RedisClient, namespace: String, capacity: Int, interval: Long, unit: TimeUnit = TimeUnit.SECONDS, minInterval: Long = 0)
                           (implicit exec: ExecutionContext){
  def key(id: String) = s"$namespace-$id"

  def tryConsume(id: String):  Future[Boolean] = {
    val now = System.nanoTime()
    val lowerWindowEdge = now - unit.toNanos(interval)

    val transaction = redis.transaction()
    transaction.zremrangebyscore(key(id), Limit(Double.MinValue), Limit(lowerWindowEdge))
    val total = transaction.zcard(id)
    val lastReqTime:Future[Seq[ByteString]] = transaction.zrevrangebyscore(id, Limit(Double.NegativeInfinity), Limit(Double.PositiveInfinity), Some(0L, 1L))
    transaction.zadd(id, (now, now))
    transaction.expire(id, unit.toSeconds(interval))
    transaction.exec()

    for {
      t <- total
      last <- lastReqTime
    } yield {
      if (last.nonEmpty) t < capacity && (now - last.head.utf8String.toLong) >= unit.toNanos(minInterval)
      else t < capacity
    }
  }

  def isBucketFull(id: String): Future[Boolean] = {
    val now = System.nanoTime()
    val lowerWindowEdge = now - unit.toNanos(interval)

    val transaction = redis.transaction()
    transaction.zremrangebyscore(key(id), Limit(Double.MinValue), Limit(lowerWindowEdge))
    val total = transaction.zcard(id)
    transaction.exec()

    for (t <- total)
      yield t == 0
  }
}
