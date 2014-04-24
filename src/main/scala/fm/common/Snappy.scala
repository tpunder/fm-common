package fm.common

import java.io.{InputStream, OutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

object Snappy {
  private val HasSnappy: Boolean = ClassUtil.classExists("org.xerial.snappy.SnappyInputStream")
  private def requireSnappy(): Unit = if (!HasSnappy) throw new ClassNotFoundException("""Snappy support missing.  Please include snappy-java:  https://github.com/xerial/snappy-java   e.g.: libraryDependencies += "org.xerial.snappy" % "snappy-java" % "1.1.0.1"""")
  
  /**
   * Create a new SnappyOutputStream
   */
  def newOutputStream(os: OutputStream): OutputStream = {
    requireSnappy()
    Impl.newOS(os)
  }
  
  /**
   * Create a new SnappyInputStream
   */
  def newInputStream(is: InputStream): InputStream = {
    requireSnappy()
    Impl.newIS(is)
  }
  
  /**
   * If Snappy is available then create a new SnappyOutputStream otherwise use a GZIPOutputStream
   */
  def newSnappyOrGzipOutputStream(os: OutputStream): OutputStream = {
    if (HasSnappy) Impl.newOS(os) else new GZIPOutputStream(os)
  }
  
  /**
   * If Snappy is available then create a new SnappyInputStream otherwise use a GZIPInputStream
   */
  def newSnappyOrGzipInputStream(is: InputStream): InputStream = {
    if (HasSnappy) Impl.newIS(is) else new GZIPInputStream(is)
  }
  
  // This is a separate object to prevent NoClassDefFoundError
  private object Impl {
    import org.xerial.snappy.{SnappyInputStream, SnappyOutputStream}
    def newOS(os: OutputStream): OutputStream = new SnappyOutputStream(os)
    def newIS(is: InputStream): InputStream = new SnappyInputStream(is)
  }
}