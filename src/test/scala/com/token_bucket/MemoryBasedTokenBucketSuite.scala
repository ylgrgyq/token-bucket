package com.token_bucket

import java.util.concurrent.TimeUnit

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MemoryBasedTokenBucketSuite extends FunSuite {

  trait TestBucket {
    val (capacity, interval, u) = (30, 5, TimeUnit.SECONDS)
    val v = new MemoryBasedTokenBucket(capacity, interval, unit = TimeUnit.SECONDS)
  }

  trait WithMinIntervalTestBucket extends TestBucket {
    val minInter = 1
    override val v = new MemoryBasedTokenBucket(capacity, interval, minInterval = minInter, unit = TimeUnit.SECONDS)
  }

  test("negative capacity") {
    val (capacity, interval) = (-1, 1)
    intercept[IllegalArgumentException] {
      new MemoryBasedTokenBucket(capacity, interval)
    }
  }

  test("zero capacity") {
    val (capacity, interval) = (0, 1)
    intercept[IllegalArgumentException] {
      new MemoryBasedTokenBucket(capacity, interval)
    }
  }

  test("negative interval"){
    val (capacity, interval) = (1, -1)
    intercept[IllegalArgumentException] {
      new MemoryBasedTokenBucket(capacity, interval)
    }
  }

  test("zero interval") {
    val (capacity, interval) = (1, 0)
    intercept[IllegalArgumentException] {
      new MemoryBasedTokenBucket(capacity, interval)
    }
  }

  test("negative minInterval"){
    val (capacity, interval, minInterval) = (1, 1, -1)
    intercept[IllegalArgumentException] {
      new MemoryBasedTokenBucket(capacity, interval, minInterval = minInterval)
    }
  }

  test("null time unit"){
    val (capacity, interval, u) = (1, 1, null)
    intercept[IllegalArgumentException] {
      new MemoryBasedTokenBucket(capacity, interval, unit = u)
    }
  }

  test("tryConsume return false after buckets is drained") {
    new TestBucket {
      val range = new Range(0, 30, 1)
      for (i <- range) {
        assert(v.tryConsume())
      }

      assert(!v.tryConsume())
    }
  }

  test("drain bucket then wait half interval the bucket should refill some tokens") {
    new TestBucket {
      val range = new Range(0, 30, 1)
      for (i <- range) {
        assert(v.tryConsume())
      }

      assert(!v.tryConsume())

      TimeUnit.NANOSECONDS.sleep(TimeUnit.SECONDS.toNanos(interval / 2))
      for (i <- new Range(1, (((interval / 2).toDouble / interval) * capacity).toInt, 1)) {
        assert(v.tryConsume())
      }

      assert(!v.tryConsume())
    }
  }

  test("interval between two consecutive consumption should bigger than minInterval") {
    new WithMinIntervalTestBucket {
      assert(v.tryConsume())
      assert(!v.tryConsume())

      TimeUnit.SECONDS.sleep(1)

      assert(v.tryConsume())
      TimeUnit.SECONDS.sleep(1)
      assert(v.tryConsume())
    }
  }
}