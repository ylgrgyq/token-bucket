package com.token_bucket


import java.util.concurrent.TimeUnit

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner


@RunWith(classOf[JUnitRunner])
class TokenBucketSuite extends FunSuite{
  test(""){
    val v = new TokenBucket(2, 2)
    val start = System.nanoTime()
    assert(v.tryGet(1))
    assert(v.tryGet(1))

    assert(! v.tryGet(1))

    TimeUnit.NANOSECONDS.sleep(TimeUnit.SECONDS.toNanos(2) - (System.nanoTime() - start))
    assert(v.tryGet(1))
  }
}
