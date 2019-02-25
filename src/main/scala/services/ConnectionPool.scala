package services

import config.RequestParams
import org.apache.commons.pool.impl.GenericObjectPool
import org.apache.directory.ldap.client.api.{DefaultLdapConnectionFactory, DefaultPoolableLdapConnectionFactory, LdapConnectionConfig, LdapConnectionPool}

class ConnectionPool {

  private val pool = new Atom[LdapConnectionPool]

  def apply(params: RequestParams): LdapConnectionPool =
    pool.computeIfAbsent {
      val factory = new DefaultLdapConnectionFactory(mkConfig(params))
      val poolConfig = new GenericObjectPool.Config()
      new LdapConnectionPool(new DefaultPoolableLdapConnectionFactory(factory), poolConfig)
    }

  private def mkConfig(params: RequestParams) = {
    val config = new LdapConnectionConfig()
    config.setLdapHost(params.host)
    config.setLdapPort(params.port)
    config.setName(params.user)
    config.setCredentials(params.pass)
    config.setUseSsl(true)
    config
  }

  private class Atom[T] {
    private var value: Option[T] = None

    def computeIfAbsent(f: => T): T =
      value.getOrElse(synchronized(value.getOrElse(update(f))))

    private def update(x: T) = {
      value = Some(x)
      x
    }
  }
}