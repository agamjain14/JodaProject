
package services

import config.RequestParams
import org.apache.directory.api.ldap.model.entry.Entry
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.ldap.client.api.{LdapConnection, LdapConnectionPool}
import org.joda.time.DateTime
import resource.{Resource, managed}

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class LdapQueryHandler(pool: ConnectionPool) extends QueryHandler {

  def query(dasId: String, params: RequestParams)(implicit ec: ExecutionContext): Future[List[DateTime]] = {
    val ldapPool = pool(params)
    implicit val resource: Resource[LdapConnection] = new LdapConnectionResource(ldapPool)

    (for {
      connection <- managed(ldapPool.getConnection)
      cursor <- managed(connection.search(params.dasBindRequest.replace("{DAS}", addPrefix(dasId)), "(objectclass=*)", SearchScope.SUBTREE, params.ldapField))
    } yield cursor.asScala.flatMap(extractDateTimeFromEntry(params)).toList).toFuture
  }

  private def addPrefix(s: String): String = (if (s.matches("A\\d+")) "AA" else "XX") + s

  private def extractDateTimeFromEntry(params: RequestParams)(entry: Entry): Option[DateTime] =
    for {
      attribute <- Option(entry.get(params.ldapField))
      value     <- Option(attribute.get())
      date      <- Try(params.ldapDateFormat.parseDateTime(value.getString)).toOption
    } yield date


  private def convertADSValue(params: RequestParams): Unit = {

  }
  private class LdapConnectionResource(pool: LdapConnectionPool) extends Resource[LdapConnection] {
    def close(connection: LdapConnection): Unit =
      pool.releaseConnection(connection)
  }
}