package fi.vm.sade.omatsivut.util

import java.util.concurrent.{ArrayBlockingQueue, ThreadFactory, ThreadPoolExecutor, TimeUnit}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object NamedThreadPoolExecutor {
  def apply(name: String, minThreads: Int, maxThreads: Int, queueSize: Int): ThreadPoolExecutor = {
    val threadFactory = new ThreadFactory {
      private val counter = new java.util.concurrent.atomic.AtomicInteger(0)
      override def newThread(r: Runnable): Thread = {
        val thread = new Thread(r)
        thread.setName(s"$name-${counter.incrementAndGet()}")
        thread.setDaemon(true)
        thread
      }
    }

    new ThreadPoolExecutor(
      minThreads,
      maxThreads,
      60L,
      TimeUnit.SECONDS,
      new ArrayBlockingQueue[Runnable](queueSize),
      threadFactory,
      new ThreadPoolExecutor.AbortPolicy()
    )
  }
}

object ThreadPools {
  val httpThreads: Int = 250
  val httpPool: ThreadPoolExecutor = NamedThreadPoolExecutor("http4s-blaze-client", httpThreads, httpThreads, 1000)
  val httpExecutionContext: ExecutionContextExecutor = ExecutionContext.fromExecutor(httpPool)
}
