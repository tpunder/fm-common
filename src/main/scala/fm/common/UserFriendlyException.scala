package fm.common

object UserFriendlyException {
  def apply(friendlyTitle: String, friendlyMessage: String): UserFriendlyException = apply(friendlyTitle, friendlyMessage, null)
  
  def apply(friendlyTitle: String, friendlyMessage: String, cause: Throwable): UserFriendlyException = SimpleUserFriendlyException(friendlyTitle, friendlyMessage, cause)
  
  /** Extract a UserFriendlyException from a Throwable (if possible) */
  def unapply(throwable: Throwable): Option[UserFriendlyException] = {
    if (null == throwable) None
    else throwable match {
      case ex: UserFriendlyException => Some(ex)
      case _ => unapply(throwable.getCause)
    }
  }

  private case class SimpleUserFriendlyException(friendlyTitle: String, friendlyMessage: String, cause: Throwable) extends UserFriendlyException(s"$friendlyTitle - $friendlyMessage", cause)
}

abstract class UserFriendlyException (message: String, cause: Throwable) extends Exception(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this(Option(cause).map{ _.toString }.orNull, cause)
  
  def friendlyTitle: String
  def friendlyMessage: String
}