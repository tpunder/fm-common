package fm.common

import fm.common.Implicits._
import java.util.Properties
import javax.mail.{Message, Session, Transport}
import javax.mail.internet.{AddressException, InternetAddress, MimeMessage}
import scala.concurrent.{ExecutionContext, Future}

final case class EmailSender (user: String, pass: String, host: String) {
  def send(to: String, from: String, bcc: Seq[String] = Nil, replyTo: String, subject: String, body: String): Unit = {
    Service.call("EmailSender", backOffStrategy = Service.BackOffStrategy.exponentialForRemote(), maxRetries = 3) {
      sendImpl(to, from, bcc, replyTo, subject, body)
    }
  }
  
  def sendAsync(to: String, from: String, bcc: Seq[String] = Nil, replyTo: String, subject: String, body: String)(implicit executionContext: ExecutionContext, timer: ScheduledTaskRunner): Future[Unit] = {
    Service.callAsync("EmailSenderAsync", backOffStrategy = Service.BackOffStrategy.exponentialForRemote(), maxRetries = 3) {
      Future { sendImpl(to, from, bcc, replyTo, subject, body) }
    }
  }
  
  private def sendImpl(to: String, from: String, bcc: Seq[String], replyTo: String, subject: String, body: String): Unit = {
    val props: Properties = new Properties
    props.put("mail.smtp.starttls.enable", "true")
    props.put("mail.smtp.host", host)
    props.put("mail.smtp.user", user)
    props.put("mail.smtp.password", pass)
    props.put("mail.smtp.port", "587")
    props.put("mail.smtp.auth", "true")

    val session: Session = Session.getDefaultInstance(props, null)
    val message: MimeMessage = new MimeMessage(session)

    message.setFrom(new InternetAddress(from))
    message.addRecipient(Message.RecipientType.TO, new InternetAddress(to))

    bcc.foreach { bcc: String =>
      message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bcc))
    }

    if(replyTo.isNotBlank) try {
      message.setReplyTo(Array(new InternetAddress(replyTo)))
    } catch {
      case ex: AddressException => // Bad replyTo Address, so don't set it
    }

    message.setSentDate(new java.util.Date)
    message.setSubject(subject)
    message.setText(body)
    
    // Not AutoCloseable
    val transport: Transport = session.getTransport("smtp")
    try {
      transport.connect(host, user, pass)
      transport.sendMessage(message, message.getAllRecipients())
    } finally {
      transport.close()
    }
  }
}