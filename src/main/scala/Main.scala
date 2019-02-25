
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.api.ldap.model.entry.Entry
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.ldap.client.api._
import org.joda.time.DateTime
import resource.{Resource, managed}

import scala.concurrent.Future
import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Success, Failure}

object Main {

  /*def main (args: Array[String] ): Unit = {
    val temp: String = "2378607203000"
    val i: DateTime = new Instant(temp).toDateTime().plusDays(90)
    println("ldt => " + i)

    val l = LocalDateTime.now()
    /*val ldt2 = LocalDateTime.ofInstant(Instant.ofEpochMilli(temp))
    println("ldt => " + ldt2)
    val ldt1 = LocalDateTime.ofInstant(Instant.ofEpochMilli(temp), ZoneOffset.UTC).plusDays(90)
    println("ldt => " + ldt1)*/
  }*/

  private class LdapConnectionResource(pool: LdapConnectionPool) extends Resource[LdapConnection] {
    def close(connection: LdapConnection): Unit =
      pool.releaseConnection(connection)
  }



  def main(args: Array[String]): Unit = {
    val pool = initConnectionPool()
    val attr = "pwdChangedTime"
    implicit val resource: Resource[LdapConnection] = new LdapConnectionResource(pool)

    val f : Future[List[String]] = (for {
      connection <- managed(pool.getConnection)
      cursor <- managed(connection.search(generateBindAddress("BE02851"), "(objectclass=*)", SearchScope.SUBTREE, attr))
//      _ <- Some(println(cursor.getMessageId))
    } yield cursor.asScala.flatMap(c => extractAttributeValueFromEntry(attr,c)).toList).toFuture
    f.onComplete {
      case Success(l) =>
        println("entering success")
        println(l.size.toString)
        l.map(println)
      case Failure(exe) =>
        println("entering failure")
        println(exe.printStackTrace())
    }
    println("Press enter to terminate")
    scala.io.StdIn.readLine()
  }
  def extractAttributeValueFromEntry(attr: String ,entry: Entry): Option[String] = {
    println("entering extractAttributeValueFromEntry")
    for {
      attribute <- Option(entry.get(attr))
      value     <- Option(attribute.get())
    } yield value.toString
  }



  private def convertToDateTime(value: String) = ???

  private def generateBindAddress(dasId: String): String = {
    val result = s"uid=$dasId,ou=dpi,ou=pi,ou=ai,ou=Identities,dc=myOrg"
    println(result)
    result
  }

  private def initConnectionPool() = {
    val factory = new DefaultLdapConnectionFactory(mkConfig())
    val poolConfig = new GenericObjectPool.Config()
    new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(factory), poolConfig)
  }


  private def mkConfig() = {
    val config = new LdapConnectionConfig()
    config.setLdapHost("dasdir.myatos.net")
    config.setLdapPort(636)
    config.setName("uid=SV000182,ou=svi,ou=ai,ou=Identities,dc=myOrg")
    config.setCredentials("MFRAylB-zmnPrpl")
    config.setUseSsl(true)
    config
  }

}