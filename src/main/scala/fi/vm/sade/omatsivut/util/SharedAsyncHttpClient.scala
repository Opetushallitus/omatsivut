package fi.vm.sade.omatsivut.util

import org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}

import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}

object SharedAsyncHttpClient {
  private val maxThreads = 250
  private val queueSize = 1000

  private val executorService = new ThreadPoolExecutor(
    maxThreads,
    maxThreads,
    60L,
    TimeUnit.SECONDS,
    new ArrayBlockingQueue[Runnable](queueSize),
    new ThreadPoolExecutor.AbortPolicy()
  )

  private val threadFactory = executorService.getThreadFactory

  private val config = new DefaultAsyncHttpClientConfig.Builder()
    .setThreadFactory(threadFactory)
    .build()

  val instance: DefaultAsyncHttpClient = new DefaultAsyncHttpClient(config)
}
