package config

import org.joda.time.format.{DateTimeFormat, DateTimeFormatter, DateTimeFormatterBuilder}

import scala.util.Try

case class RequestParams(
                          host: String,
                          port: Int,
                          user: String,
                          pass: String,
                          dasBindRequest: String,
                          criticalDays: Int,
                          mediumDays: Int,
                          highDays: Int,
                          veryHighDays: Int,
                          ldapDateFormat: DateTimeFormatter,
                          validityPeriod: Int,
                          ldapField: String,
                          itemTitle: String,
                          itemDescription: String,
                          externalLink: String
                        )

object RequestParams {

  def read(params: Map[String, String]): Try[RequestParams] =
    for {
      host            <- Try(params("host"))
      port            <- Try(params("port")).map(_.toInt)
      user            <- Try(params("ncf-bind-request"))
      pass            <- Try(params("ncf-password"))
      dasBindRequest  <- Try(params("das-bind-request"))
      criticalDays    <- Try(params("critical-days-before-expiration")).map(_.toInt)
      mediumDays      <- Try(params("medium-days")).map(_.toInt)
      highDays        <- Try(params("high-days")).map(_.toInt)
      veryHighDays    <- Try(params("very-high-days")).map(_.toInt)
      ldapDateFormat  <- Try(params("ldap-date-format")).map(createDateTimeFormat)
      ldapField       <- Try(params("ldap-field-name"))
      itemTitle       <- Try(params("item-title"))
      itemDescription <- Try(params("item-description"))
      externalLink    <- Try(params("external-link"))
      validityPeriod <- Try(params("validity-period")).map(_.toInt)
    } yield RequestParams(host, port, user, pass, dasBindRequest,
      criticalDays, mediumDays, highDays,
      veryHighDays, ldapDateFormat, validityPeriod, ldapField,
      itemTitle, itemDescription, externalLink)

  private def createDateTimeFormat(s: String) = {
    val parsers = s.split(",").map(DateTimeFormat.forPattern).map(_.getParser)
    new DateTimeFormatterBuilder().append(None.orNull, parsers).toFormatter
  }

}