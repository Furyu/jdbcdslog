package org.jdbcdslog

import org.specs2.mutable._
import org.scalaquery.meta.MTable

object ScalaQueryIntegrationSpec extends Specification {

  "test" should {
    "test" in {

      import org.scalaquery.ql._
      import org.scalaquery.ql.TypeMapper._
      import org.scalaquery.session.{Database, Session}
      import org.scalaquery.ql.extended.{ExtendedProfile, ExtendedTable => Table, MySQLDriver}

      case class Post(id: Long, title: String)

      // Load the driver and execute the static initializer within it.
//      Class.forName("org.jdbcdslog.DriverLoggingProxy")

      val db = Database.forURL("jdbc:jdbcdslog:mysql://localhost:3306/test?targetDriver=com.mysql.jdbc.Driver")

      val driver = MySQLDriver

      import driver.Implicit._

      object Posts extends Table[Post]("posts") {
        def id = column[Long]("id")
        def title = column[String]("title")
        def * : ColumnBase[Post] = id ~ title <> (Post.apply _, Post.unapply _)

        def insert(p: Post)(implicit session: Session) = (id ~ title) insertAll ((p.id, p.title))
        def all(implicit session: Session) = {
          val q = for {
            p <- Posts if p.id === 1L
          } yield p
          q.list
        }
      }

      db.withSession { implicit session: Session =>
        val tableNames = MTable.getTables.list().map(_.name.name)
        println(tableNames)
        if (tableNames.contains(Posts.tableName)) {
          Posts.ddl.drop
        }
        Posts.ddl.create
      }

      db.withSession { implicit session: Session =>
         Posts.insert(Post(1L, "one"))
      }

      db.withSession { implicit session: Session =>
        Posts.all

        val q = for {
          p <- Posts if p.id === 1L
        } yield p.title

        q.update("one-modified")
      }

      success

    }
  }


}
