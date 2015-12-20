package com.token_bucket


import java.util.concurrent.TimeUnit

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class MemoryBasedTokenBucketSuite extends FunSuite{

  test("tryConsume return false after buckets is drained"){
    val capacity = 30
    val interval = 5
    val v = new MemoryBasedTokenBucket(capacity, interval, unit = TimeUnit.SECONDS)

    val range = new Range(0, 30, 1)
    for (i <- range) {
      assert(v.tryConsume())
    }

    assert(! v.tryConsume())
  }

  test("drain bucket then wait half interval the bucket should refill some tokens"){
    val capacity = 30
    val interval = 5
    val v = new MemoryBasedTokenBucket(capacity, interval, unit = TimeUnit.SECONDS)

    val range = new Range(0, 30, 1)
    for (i <- range) {
      assert(v.tryConsume())
    }

    assert(! v.tryConsume())

    TimeUnit.NANOSECONDS.sleep(TimeUnit.SECONDS.toNanos(interval / 2))
    for (i <- new Range(1, (((interval / 2).toDouble / interval) * capacity).toInt, 1)){
      assert(v.tryConsume())
    }

    assert(! v.tryConsume())
  }
}
