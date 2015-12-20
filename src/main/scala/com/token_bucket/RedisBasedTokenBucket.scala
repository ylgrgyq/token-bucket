package com.token_bucket

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import redis.RedisClient
import redis.api.Limit

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * Created on 15/12/14.
  * Author: ylgrgyq
  */

class RedisBasedTokenBucket(namespace: String, id: String, capacity: Int, interval: Long, minInterval: Long, unit: TimeUnit, redis: RedisClient)(implicit exec: ExecutionContext) extends TokenBucket{
  private val redisKey = s"$namespace-$id"

  override def tryConsume(tokenInNeed: Int) = {
    throw new UnsupportedOperationException
  }

  override def tryConsume(): Boolean = {
    val now = System.nanoTime()
    val lowerWindowEdge = now - unit.toNanos(interval)

    val transaction = redis.transaction()
    transaction.zremrangebyscore(redisKey, Limit(Double.MinValue), Limit(lowerWindowEdge))
    val total = transaction.zcard(id)
    val lastReqTime:Future[Seq[ByteString]] = transaction.zrevrangebyscore(id, Limit(Double.NegativeInfinity), Limit(Double.PositiveInfinity), Some(0L, 1L))
    transaction.zadd(id, (now, now))
    transaction.expire(id, unit.toSeconds(interval))
    transaction.exec()

    val ret:Future[Boolean] = for {
      t <- total
      last <- lastReqTime
    } yield {
      if (last.nonEmpty) t < capacity && (now - last.head.utf8String.toLong) >= unit.toNanos(minInterval)
      else t < capacity
    }

    Await.result(ret, 5.seconds)
  }
}
