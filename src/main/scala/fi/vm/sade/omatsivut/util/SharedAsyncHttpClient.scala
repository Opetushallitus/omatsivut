package fi.vm.sade.omatsivut.util

import org.asynchttpclient.{DefaultAsyncHttpClient, DefaultAsyncHttpClientConfig}

object SharedAsyncHttpClient {
  private val config = new DefaultAsyncHttpClientConfig.Builder()
    .setMaxConnections(100)
    .setMaxConnectionsPerHost(20)
    .setThreadFactory(ThreadPools.httpPool.getThreadFactory)
    .build()

  val instance: DefaultAsyncHttpClient = new DefaultAsyncHttpClient(config)
}
