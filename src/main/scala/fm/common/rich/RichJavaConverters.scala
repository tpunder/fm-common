package fm.common.rich

import scala.collection.JavaConverters._
import scala.collection._

object RichJavaConverters {
  implicit class RichJavaList[A](val l: java.util.List[A]) extends AnyVal {
    def asScalaNullToEmpty(): mutable.Buffer[A] = {
      if (l == null) mutable.ArrayBuffer.empty
      else l.asScala
    }
  }

  implicit class RichJavaIterator[A](val i: java.util.Iterator[A]) extends AnyVal {
    def asScalaNullToEmpty(): Iterator[A] = {
      if (i == null) Iterator.empty
      else i.asScala
    }
  }

  implicit class RichJavaSet[A, B](val s: java.util.Set[A]) extends AnyVal {
    def asScalaNullToEmpty(): mutable.Set[A] = {
      if (s == null) mutable.Set.empty
      else s.asScala
    }
  }

  implicit class RichJavaCollection[A, B](val c: java.util.Collection[A]) extends AnyVal {
    def asScalaNullToEmpty(): Iterable[A] = {
      if (c == null) Iterable.empty
      else c.asScala
    }
  }

  implicit class RichJavaDictionary[A, B](val m: java.util.Dictionary[A, B]) extends AnyVal {
    def asScalaNullToEmpty(): mutable.Map[A, B] = {
      if (m == null) mutable.Map.empty
      else m.asScala
    }
  }

  implicit class RichJavaEnumeration[A, B](val e: java.util.Enumeration[A]) extends AnyVal {
    def asScalaNullToEmpty(): Iterator[A] = {
      if (e == null) Iterator.empty
      else e.asScala
    }
  }

  implicit class RichJavaIterable[A, B](val i: java.lang.Iterable[A]) extends AnyVal {
    def asScalaNullToEmpty(): Iterable[A] = {
      if (i == null) Iterable.empty
      else i.asScala
    }
  }

  implicit class RichJavaConcurrentMap[A, B](val m: java.util.concurrent.ConcurrentMap[A, B]) extends AnyVal {
    def asScalaNullToEmpty(): collection.concurrent.Map[A, B] = {
      if (m == null) collection.concurrent.TrieMap.empty
      else m.asScala
    }
  }

  implicit class RichJavaMap[A, B](val m: java.util.Map[A, B]) extends AnyVal {
    def asScalaNullToEmpty(): mutable.Map[A, B] = {
      if (m == null) mutable.Map.empty
      else m.asScala
    }
  }

  implicit class RichJavaProperties(val p: java.util.Properties) extends AnyVal {
    def asScalaNullToEmpty(): mutable.Map[String, String] = {
      if (p == null) mutable.Map.empty
      else p.asScala
    }
  }
}