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

class RedisBasedTokenBucket(redis: RedisClient, namespace: String, capacity: Int, interval: Long, unit: TimeUnit = TimeUnit.SECONDS, minInterval: Long = 0)
                           (implicit exec: ExecutionContext) extends GropuedTokenBucket{
  override def tryConsume(id: String, tokenInNeed: Int) = {
    throw new UnsupportedOperationException
  }

  def key(id: String) = s"$namespace-$id"

  override def tryConsume(id: String): Boolean = {
    val now = System.nanoTime()
    val lowerWindowEdge = now - unit.toNanos(interval)

    val transaction = redis.transaction()
    transaction.zremrangebyscore(key(id), Limit(Double.MinValue), Limit(lowerWindowEdge))
    val total = transaction.zcard(id)
    val lastReqTime:Future[Seq[ByteString]] = transaction.zrevrangebyscore(id, Limit(Double.NegativeInfinity), Limit(Double.PositiveInfinity), Some(0L, 1L))
    transaction.zadd(id, (now, now))
    transaction.expire(id, unit.toSeconds(interval))
    transaction.exec()

    val ret:Future[Boolean] =
      for {
        t <- total
        last <- lastReqTime
      } yield {
        if (last.nonEmpty) t < capacity && (now - last.head.utf8String.toLong) >= unit.toNanos(minInterval)
        else t < capacity
      }

    Await.result(ret, 5.seconds)
  }

  override def isBucketFull(id: String): Boolean = {
    val now = System.nanoTime()
    val lowerWindowEdge = now - unit.toNanos(interval)

    val transaction = redis.transaction()
    transaction.zremrangebyscore(key(id), Limit(Double.MinValue), Limit(lowerWindowEdge))
    val total = transaction.zcard(id)
    transaction.exec()

    val ret:Future[Boolean] =
      for (t <- total)
      yield t == 0

    Await.result(ret, 5.seconds)
  }
}
