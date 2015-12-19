package com.token_bucket


import java.util.concurrent.TimeUnit

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class SynchronizedTokenBucketSuite extends FunSuite{
  test(""){
    val p = new FixedRateTokenSupplyPolicy(2, 2, TimeUnit.SECONDS)
    val v = new MemoryBasedTokenBucket(2, 2, p)
    val start = System.nanoTime()
    assert(v.tryConsume(1))
    assert(v.tryConsume(1))

    assert(! v.tryConsume(1))

    TimeUnit.NANOSECONDS.sleep(TimeUnit.SECONDS.toNanos(2) - (System.nanoTime() - start))
    assert(v.tryConsume(1))
  }
}
