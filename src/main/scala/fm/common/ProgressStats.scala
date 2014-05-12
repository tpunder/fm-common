package fm.common

import java.util.concurrent.atomic.AtomicLong

object ProgressStats {
  // TODO: better naming for this
  def forFasterProcesses() = new ProgressStats(dotPer = 10000L, statsPer = 250000L)
}

final case class ProgressStats(dotPer: Long = 1000L, statsPer: Long = 25000L, logFinalStats: Boolean = false) extends Logging {
  private[this] val startTimeMillis: Long = System.currentTimeMillis
  private[this] val _count = new AtomicLong(0)
  
  @volatile private[this] var sectionStartTime: Long = startTimeMillis
  @volatile private[this] var batchStartTimeMillis: Long = startTimeMillis  
  @volatile var hide = !logger.isInfoEnabled // Default to printing only if info logging is enabled for ProgressStats

  def reset(): Unit = {
    _count.set(0)
    sectionStartTime = System.currentTimeMillis
    batchStartTimeMillis = sectionStartTime
  } 
  
  def count: Long = _count.get
  
  def increment(): Unit = {
    val c: Long = _count.incrementAndGet

    if (c % dotPer == 0) {
      if (!hide) print(".")
    }

    if (c % statsPer == 0) {
      if (!hide) {
        print(c)
        printRecordsPerSecondInfo(batchStartTimeMillis, statsPer)
      }
      batchStartTimeMillis = System.currentTimeMillis
    }
  }

  def sectionStats(): Unit = {
    if (hide) return

    val c: Long = _count.get

    println
    if (c > 0L) {
      print(s"Total Count: $c")
      printRecordsPerSecondInfo(startTimeMillis, c)
      print("  |  ")
      sectionTime
      
      println
    }
  }
  
  def finalStats(): Unit = {
    sectionStats
    
    if (sectionStartTime != startTimeMillis) finalTime
  }
  
  def sectionTime(): Unit = {
    printTime("Time", sectionStartTime)
  }
  
  def finalTime(): Unit = {    
    printTime("Total Time", startTimeMillis)
  }
  
  def printRecordsPerSecondInfo(time: Long, records: Long) {
    val totalTimeMillis: Long = System.currentTimeMillis - time
    val recordsPerSecond: Long = (records.toDouble / totalTimeMillis.toDouble * 1000.toDouble).toLong
    if (!hide) print(" ("+recordsPerSecond+"/s)")
  }
  
  private def printTime(title: String, time: Long) {
    if (hide) return
    val totalTimeSecs: Long = (System.currentTimeMillis - time) / 1000
    val msg: String = title+": "+totalTimeSecs+" seconds ("+((totalTimeSecs/60d*100).toInt/100d)+" minutes) ("+((totalTimeSecs/3600d*100).toInt/100d)+" hours)"
    println(msg)
    if (logFinalStats) logger.info(msg)
  }
}