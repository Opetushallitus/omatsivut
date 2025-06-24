package fi.vm.sade.omatsivut.util

import org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}

object SharedAsyncHttpClient {
  private val config = new DefaultAsyncHttpClientConfig.Builder()
    .setThreadFactory(ThreadPools.httpPool.getThreadFactory)
    .build()

  val instance: DefaultAsyncHttpClient = new DefaultAsyncHttpClient(config)
}
