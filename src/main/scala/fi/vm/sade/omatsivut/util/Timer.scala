package fi.vm.sade.omatsivut.util

object Timer extends Logging {
  def timed[R](block: => R, thresholdMs: Int = 0, blockname: String = ""): R = {
    val t0 = System.nanoTime()
    val result = block
    val t1 = System.nanoTime()
    val time: Long = (t1 - t0) / 1000000
    if (time >= thresholdMs) logger.info(blockname + " call took: " + time + " ms")
    result
  }
}
