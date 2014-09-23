package fm.common

/**
 * This is a replacement for the deprecated scala.collection.SeqProxy.
 * 
 * This doesn't implement Seq[A] like the original SeqProxy but instead
 * just provides an implicit conversion from this trait into a Seq[A] which
 * then gives you all the Seq functionality
 */
trait SeqProxy        [A]   { def self: Seq[A]         }
trait IterableProxy   [A]   { def self: Iterable[A]    }
trait TraversableProxy[A]   { def self: Traversable[A] }
trait IndexedSeqProxy [A]   { def self: IndexedSeq[A]  }
trait SetProxy        [A]   { def self: Set[A]         }
trait MapProxy        [K,V] { def self: Map[K,V]       }

object SeqProxy         { implicit def convert[A]  (p: SeqProxy[A])        : Seq[A]         = p.self }
object IterableProxy    { implicit def convert[A]  (p: IterableProxy[A])   : Iterable[A]    = p.self }
object TraversableProxy { implicit def convert[A]  (p: TraversableProxy[A]): Traversable[A] = p.self }
object IndexedSeqProxy  { implicit def convert[A]  (p: IndexedSeqProxy[A]) : IndexedSeq[A]  = p.self }
object SetProxy         { implicit def convert[A]  (p: SetProxy[A])        : Set[A]         = p.self }
object MapProxy         { implicit def convert[K,V](p: MapProxy[K,V])      : Map[K,V]       = p.self }