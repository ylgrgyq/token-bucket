package com.token_bucket

import java.util.concurrent.TimeUnit

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MemBasedTokenBucketSuite extends FunSuite {

  trait TestBucket {
    val (capacity, tokensPerInterval, interval, u) = (30, 10, 3, TimeUnit.SECONDS)
    val v = MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).tokensPerInterval(tokensPerInterval).interval(interval, u).payForFailedTry(false).build()
  }

  trait WithMinIntervalTestBucket extends TestBucket {
    val minInter = 1
    override val v = new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval, TimeUnit.SECONDS).minInterval(minInter, TimeUnit.SECONDS).build()
  }

  test("negative capacity") {
    val (capacity, interval) = (-1, 1)
    intercept[IllegalArgumentException] {
      new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval)
    }
  }

  test("zero capacity") {
    val (capacity, interval) = (0, 1)
    intercept[IllegalArgumentException] {
      new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval)
    }
  }

  test("negative interval"){
    val (capacity, interval) = (1, -1)
    intercept[IllegalArgumentException] {
      new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval)
    }
  }

  test("zero interval") {
    val (capacity, interval) = (1, 0)
    intercept[IllegalArgumentException] {
      new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval)
    }
  }

  test("negative minInterval"){
    val (capacity, interval, minInterval) = (1, 1, -1)
    intercept[IllegalArgumentException] {
      new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval).minInterval(minInterval)
    }
  }

  test("null time unit"){
    val (capacity, interval, u) = (1, 1, null)
    intercept[IllegalArgumentException] {
      new MemBasedTokenBucket.MemBasedTokenBucketBuilder().capacity(capacity).interval(interval, u)
    }
  }

  test("try consume zero tokens"){
    new TestBucket {
      intercept[IllegalArgumentException] {
        v.tryConsume(0)
      }
    }
  }

  test("try consume negative tokens"){
    new TestBucket {
      intercept[IllegalArgumentException] {
        v.tryConsume(-1)
      }
    }
  }

  test("tryConsume return false after buckets is drained") {
    new TestBucket {
      for (i <- 0 until capacity) {
        assert(v.tryConsume())
      }

      assert(!v.tryConsume())
    }
  }

  test("drain bucket then wait an interval the bucket should refill tokenPerPeriod tokens") {
    new TestBucket {
      val range = new Range(0, capacity, 1)
      for (i <- range) {
        assert(v.tryConsume())
      }

      assert(!v.tryConsume())

      TimeUnit.NANOSECONDS.sleep(TimeUnit.SECONDS.toNanos(interval))
      for (i <- 0 until tokensPerInterval){
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

  test("drain bucket twice then wait half of interval leave half capacity debt to pay") {
    new TestBucket {
      for (i <- 0 until capacity) {
        assert(v.tryConsume())
      }

      for (i <- 0 until capacity) {
        assert(! v.tryConsume())
      }

      u.sleep(interval / 2)
      assert(! v.tryConsume())
    }
  }
}
