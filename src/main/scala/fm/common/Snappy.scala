package fm.common

import java.io.{InputStream, OutputStream}

object Snappy {
  private val HasSnappy: Boolean = ClassUtil.classExists("org.xerial.snappy.SnappyInputStream")    
  private def requireSnappy(): Unit = if (!HasSnappy) throw new ClassNotFoundException("""Snappy support missing.  Please include snappy-java:  https://github.com/xerial/snappy-java   e.g.: libraryDependencies += "org.xerial.snappy" % "snappy-java" % "1.1.0.1"""")
  
  def newOutputStream(os: OutputStream): OutputStream = {
    requireSnappy()
    new org.xerial.snappy.SnappyOutputStream(os)
  }
  
  def newInputStream(is: InputStream): InputStream = {
    requireSnappy()
    new org.xerial.snappy.SnappyInputStream(is)
  }
}