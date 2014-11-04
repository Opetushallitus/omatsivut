package fi.vm.sade.omatsivut.memoize

import com.google.common.cache.{Cache => GuavaCache, CacheStats, CacheBuilder}
import java.util.concurrent.{Callable, TimeUnit}

sealed trait Caching[K , V] {
  def get(k: K): Option[V]
  def put(k: K, v: V)
  def getOrElseUpdate(k: K, f: () => V): V
}

class Cache[K, V](cache: GuavaCache[K,V]) extends Caching[K, V] {
  def get(k: K): Option[V] = {
    Option(cache.getIfPresent(k))
  }

  def getOrElseUpdate(k: K, f: () => V): V = {
    cache.get(k, new Callable[V] {
      def call(): V = f()
    })
  }

  def put(k: K, v: V) {
    cache.put(k, v)
  }

  def remove(k: K) {
    cache.invalidate(k)
  }

  def clear() {
    cache.invalidateAll()
  }

  def size: Long = {
    cache.size()
  }

  def stats: CacheStats = {
    cache.stats()
  }
}

object TTLCache {
  /**
   * Builds a TTL Cache store
   *
   * @param duration the TTL in seconds
   * @tparam K
   * @tparam V
   */
  def apply[K, V](duration: Long, maxSize: Int) = {
    val ttlCache: GuavaCache[K, V] =
      CacheBuilder
        .newBuilder()
        .recordStats()
        .expireAfterWrite(duration, TimeUnit.SECONDS)
        .maximumSize(maxSize)
        .build().asInstanceOf[GuavaCache[K, V]]
    new Cache[K, V](ttlCache)
  }
}




