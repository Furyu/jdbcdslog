package jp.furyu.play.jdbcdslog

import jp.furyu.jdbcdslog.fluent.Context
import scala.collection.JavaConverters._
import org.fluentd.logger.FluentLogger

/**
 * Actionから受け取ったログをfluentに
 * 渡すためのクラス
 */
class AccessLogger(fluentLogger: FluentLogger, label: String) {

  def log(context: Context){
    fluentLogger.log(label, context.toMap.asJava)
  }

}

object AccessLogger {

  def apply(tag: String = "debug", host: String = "localhost", port: Int = 24224, label: String = "test"): AccessLogger =
    new AccessLogger(FluentLogger.getLogger(tag, host, port), label)

  lazy val default = {
    val conf = play.api.Play.current.configuration
    val tag = conf.getString("access_logger.tag").getOrElse("debug")
    val host = conf.getString("access_logger.host").getOrElse("localhost")
    val port = conf.getInt("access_logger.port").getOrElse(24224)
    val label = conf.getString("access_logger.label").getOrElse("test")
    AccessLogger(tag, host, port, label)
  }

}
