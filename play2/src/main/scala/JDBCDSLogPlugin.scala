package jp.furyu.play.jdbcdslog

import play.api.Plugin
import com.github.furyu.jdbcdslog.fluent.ConnectionProvider
import java.sql.Connection
import play.api.db.BoneCPPlugin

/**
 * The jdbcdslog plugin which records every sql statements.
 *
 * There are three configuration you must done in order to use this plugin.
 *
 * 1. You must start the JDBCDSLogPlugin before play's DBPlugin(BoneCP based one)
 *
 * In other words, assuming Play's default play.plugins being:
 *
 *   100:play.api.i18n.MessagesPlugin
 *   200:play.api.db.BoneCPPlugin
 *   300:play.db.ebean.EbeanPlugin
 *   400:play.db.jpa.JPAPlugin
 *   500:play.api.db.evolutions.EvolutionsPlugin
 *   600:play.api.cache.EhCachePlugin
 *   1000:play.api.libs.concurrent.AkkaPlugin
 *   10000:play.api.GlobalPlugin
 *
 * You must add the following to your application's conf/play.plugins:
 *
 *   201:jp.furyu.play.jdbcdslog.JDBCDSLogPlugin
 *
 * 2. You must have at least two datasources configured in application.conf,
 * One for connections through jdbcdslog, one for direct connections via common JDBC drivers.
 *
 * Assuming that your application requires exactly one datasource, your application.conf would look like:
 *
 *   # 1. log every statements executed through the `default` datasource.
 *   db.default.driver=org.jdbcdslog.DriverLoggingProxy
 *   db.default.url="jdbc:jdbcdslog:mysql://cd-db-test.ck5ls8mac0rf.ap-northeast-1.rds.amazonaws.com:3306/cardgamedevdb?targetDriver=com.mysql.jdbc.Driver"
 *   db.default.user=cardgame
 *   db.default.password=c4rdg4m3
 *
 *   # 2. the datasource used by by jdbcdslog-fluent to look up the information about tables and columns, to
 *   # normalize SQL statements not to contain aliases in field names in `WHERE` and `SET` clauses.
 *   #
 *   # Your application itself would not need, and has no need for this datasource.
 *   #
 *   db.mysql.driver=com.mysql.jdbc.Driver
 *   db.mysql.url="jdbc:mysql://cd-db-test.ck5ls8mac0rf.ap-northeast-1.rds.amazonaws.com:3306/cardgamedevdb"
 *   db.mysql.user=cardgame
 *   db.mysql.password=c4rdg4m3
 *
 * 3. Put `jdbcdslog.properties` file anywhere in the classpath
 *
 *   jdbcdslog.pluginClassName=com.github.furyu.jdbcdslog.fluent.FluentEventHandler
 *
 *
 * @param app Running Play application
 */
class JDBCDSLogPlugin(app: play.api.Application) extends Plugin {
  override def onStart() {
    com.github.furyu.jdbcdslog.fluent.ConnectionProvider.provider = Option(
      new ConnectionProvider {
        // `url` would be something other than jdbcdslog one.
        // E.g. if you use jdbcdslog with MySQL, you get "jdbc:mysql://" for 'url', not "jdbcdslog:mysql://".
        def withConnection[T](url: String)(block: (Connection) => T): T = {
          // BoneCPPlugin should be initialized until now
          val dbPlugin = play.api.Play.current.plugin[BoneCPPlugin].get
          val dbApi = dbPlugin.api
          assert(dbApi.datasources.size > 0)
          val urlToDsName = dbApi.datasources.map {
            case (_, name) =>
              val url = dbApi.withConnection(name)(_.getMetaData.getURL)
              // e.g. "mysql://host/db" -> "default" mapping
              url -> name
          }.toMap
          // e.g. "default" -> jdbcdslog's ConnectionLoggingProxy's datasource
          //
          // Notice that if we use jdbcdslog in combination with MySQL for example,
          // we are implicitly mapping url ("mysql://") to DB name (e.g. "default"),
          // to jdbcdslog's DriverLoggingProxy.
          dbPlugin.api.withConnection(urlToDsName(url))(block)
        }
      }
    )

  }
}
