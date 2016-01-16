package com.token_bucket

import java.nio.ByteOrder
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import redis.RedisClient
import redis.api.Limit

import scala.concurrent.Await

/**
  * Created on 15/12/16.
  * Author: ylgrgyq
  */
@RunWith(classOf[JUnitRunner])
class RedisBasedTokenBucketSuite extends FunSuite {
  test("") {


    //    implicit val akkaSystem = akka.actor.ActorSystem()

    import scala.concurrent.ExecutionContext.Implicits._
    import scala.concurrent.duration._

    //    val redis = RedisClient()
    //    val bucket = new RedisBasedTokenBucket("hahaha", 20, 1, TimeUnit.MINUTES, 20, redis)
    //    assert(bucket.tryConsume())

    //    val a = 7L
    //    val b = 3L
    //    println(a / b)
    //    println(Math.ceil(a.toDouble / b))
    //    println(Math.ceil(a.toDouble / b).toLong)

  }
}



