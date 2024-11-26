package fi.vm.sade.omatsivut.util

import java.io.IOException
import java.net.{URL, URLConnection, URLStreamHandler}

class ClassPathUrlHandler(val classLoader: ClassLoader = getClass.getClassLoader) extends URLStreamHandler {
  @throws[IOException]
  protected def openConnection(u: URL): URLConnection = {
    val resourceUrl = classLoader.getResource(u.getPath)
    resourceUrl.openConnection
  }
}
