package com.token_bucket

import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

import akka.actor.{ActorSystem, ActorRefFactory}
import akka.util.ByteString
import redis.RedisClient
import redis.api.Limit

import scala.concurrent.{ExecutionContext, Future, Await}
import scala.concurrent.duration._

/**
  * Created on 15/12/14.
  * Author: ylgrgyq
  */

class RedisBasedTokenBucket(id: String, capacity: Int, interval: Long, unit: TimeUnit, minInterval: Long, redis: RedisClient) extends TokenBucket{

  def tryConsume(tokenInNeed: Int) = {
    throw new UnsupportedOperationException
  }

  def tryConsume()(implicit exec: ExecutionContext): Boolean = {
    val now = System.nanoTime()
    val lowerWindowEdge = now - unit.toNanos(interval)

    val transaction = redis.transaction()
    transaction.zremrangebyscore(id, Limit(Double.MinValue), Limit(lowerWindowEdge))
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
