package jp.furyu.jdbcdslog.fluent

import org.specs2.mutable._
import com.github.stephentu.scalasqlparser._
import com.github.stephentu.scalasqlparser.IntType
import jp.furyu.jdbcdslog.fluent.Context
import com.github.stephentu.scalasqlparser.Definitions
import com.github.stephentu.scalasqlparser.TableColumn
import scala.Some

class FluentMarshallerSpec extends Specification {

  val marshaller = new FluentMarshaller
  val definitions = new Definitions(
    Map(
      "items" -> Seq(
        TableColumn("id", IntType(8)),
        TableColumn("value", IntType(8))
      ),
      "users" -> Seq(
        TableColumn("id", IntType(8)),
        TableColumn("name", VariableLenString(100))
      ),
      "user_items" -> Seq(
        TableColumn("user_id", IntType(8)),
        TableColumn("item_id", IntType(8)),
        TableColumn("version", IntType(8)),
        TableColumn("quantity", IntType(3)),
        TableColumn("updated_date", DateType)
      )
    ))
  val dateString = "2012/12/28 13:08:00"
  val dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
  val date = dateFormat.parse(dateString)

  def toJava(anyRef: AnyRef) = {
    anyRef match {
      case scalaMap: Map[_, _] =>
        toJavaMap(scalaMap.asInstanceOf[Map[String, AnyRef]])
      case scalaArray: Array[_] =>
        toJavaArray(scalaArray.asInstanceOf[Array[AnyRef]])
      case _ =>
        anyRef
    }
  }

  def toJavaArray(ary: Array[AnyRef]): java.util.List[AnyRef] = {
    val jary = new java.util.ArrayList[AnyRef]()
    for { a <- ary } jary.add(toJava(a))
    jary
  }

  def toJavaMap(map: Map[String, AnyRef]): java.util.Map[String, AnyRef] = {
    val jmap = new java.util.HashMap[String, AnyRef]()
    for {
      (a,b) <- map
    } jmap.put(a, toJava(b))
    jmap
  }

  def getResult(sqlStatement: String) = {
    marshaller.marshal(
      sql = sqlStatement,
      schema = definitions,
      context = Some(
        new Context {
          def toMap: Map[String, AnyRef] = {
            Map(
              "a" -> "b",
              "timestamp" -> date.getTime().asInstanceOf[AnyRef]
            )
          }
        }
      ),
      time = 123L)
  }

  "FluentMarshaller" should {
    "marshal SELECT statements" in {
      val result = marshaller.marshal(
        sql = "select value from items where id = 1",
        schema = definitions,
        context = Some(
          new Context {
            def toMap: Map[String, AnyRef] = {
              Map(
                "a" -> "b",
                "timestamp" -> date.getTime().asInstanceOf[AnyRef]
              )
            }
          }
        ),
        time = 123L)

      val expected = {
        val db = Map(
          "command" -> "select",
          "projections" -> Array(
            Map(
              "expr" -> Map(
                "field" -> "`items`.`value`"
              )
            )
          ),
          "relations" -> Map(
            "items" -> "items"
          ),
          "where" -> Map(
            "`items`.`id`" -> 1
          )
        )
        val expected = Map(
          "db" -> db,
          "a" -> "b",
          "timestamp" -> date.getTime.asInstanceOf[AnyRef],
          "timing" -> 123L.asInstanceOf[AnyRef]
        )
        toJavaMap(expected)
      }
      result.toString must be equalTo (expected.toString)
    }
    "marshal INSERT statements" in {
      val insert1 = "insert into items values (1, 2);"
      val insert2 = "insert into items (value, id) values (2, 1);"
      val insert3 = "insert into items (items.value, items.id) values (2, 1);"
      val insert4 = "insert into items (`items`.`value`, `items`.`id`) values (2, 1);"

      def getResult(insert: String) = {
        marshaller.marshal(
          sql = insert,
          schema = definitions,
          context = Some(
            new Context {
              def toMap: Map[String, AnyRef] = {
                Map(
                  "a" -> "b",
                  "timestamp" -> date.getTime().asInstanceOf[AnyRef]
                )
              }
            }
          ),
          time = 123L)
      }

      val expected = {
        val db = Map(
          "command" -> "insert",
          "table" -> "items",
          "values" -> Map(
            "id" -> 1,
            "value" -> 2
          )
        )
        val expected = Map(
          "db" -> db,
          "a" -> "b",
          "timestamp" -> date.getTime.asInstanceOf[AnyRef],
          "timing" -> 123L.asInstanceOf[AnyRef]
        )
        toJavaMap(expected)
      }

      getResult(insert1).toString aka ("insert1") must be equalTo (expected.toString)
      getResult(insert2).toString aka ("insert2") must be equalTo (expected.toString)
      getResult(insert3).toString aka ("insert3") must be equalTo (expected.toString)
      getResult(insert4).toString aka ("insert4") must be equalTo (expected.toString)
    }
    "marshal UPDATE statements #1" in {
      val update1 = "update user_items set quantity = 1, updated_date = '2013/01/18 09:30:00' where user_id = 1 and item_id = 2;"
      val update2 = "update user_items set user_items.quantity = 1, user_items.updated_date = '2013/01/18 09:30:00' where user_items.user_id = 1 and user_items.item_id = 2;"
      val update3 = "update user_items set `user_items`.`quantity` = 1, `user_items`.`updated_date` = '2013/01/18 09:30:00' where `user_items`.`user_id` = 1 and `user_items`.`item_id` = 2;"

      val expected = {
        val db = Map(
          "command" -> "update",
          "relations" -> Map(
            "user_items" -> "user_items"
          ),
          "values" -> Map(
            "`user_items`.`quantity`" -> 1,
            "`user_items`.`updated_date`" -> "2013/01/18 09:30:00"
          ),
          "where" -> Map(
            "and" -> Map(
              "lhs" -> Map("`user_items`.`user_id`" -> 1),
              "rhs" -> Map("`user_items`.`item_id`" -> 2)
            )
          )
        )
        val expected = Map(
          "db" -> db,
          "a" -> "b",
          "timestamp" -> date.getTime.asInstanceOf[AnyRef],
          "timing" -> 123L.asInstanceOf[AnyRef]
        )
        toJavaMap(expected)
      }

      getResult(update1).toString aka ("update1") must be equalTo (expected.toString)
      getResult(update2).toString aka ("update2") must be equalTo (expected.toString)
      getResult(update3).toString aka ("update3") must be equalTo (expected.toString)
    }
    "marshal UPDATE statements #2" in {
      val update1 = "update user_items set quantity = 1, updated_date = '2013/01/18 09:30:00', version = 2 where user_id = 1 and item_id = 2 and version = 1;"
      val update2 = "update user_items set user_items.quantity = 1, user_items.updated_date = '2013/01/18 09:30:00', user_items.version = 2 where user_items.user_id = 1 and user_items.item_id = 2 and user_items.version = 1;"
      val update3 = "update user_items set `user_items`.`quantity` = 1, `user_items`.`updated_date` = '2013/01/18 09:30:00', `user_items`.`version` = 2 where `user_items`.`user_id` = 1 and `user_items`.`item_id` = 2 and `user_items`.`version` = 1;"

      val expected = {
        val db = Map(
          "command" -> "update",
          "relations" -> Map(
            "user_items" -> "user_items"
          ),
          "values" -> Map(
            "`user_items`.`quantity`" -> 1,
            "`user_items`.`updated_date`" -> "2013/01/18 09:30:00",
            "`user_items`.`version`" -> 2
          ),
          "where" -> Map(
            "and" -> Map(
              "lhs" -> Map(
                "and" -> Map(
                  "lhs" -> Map("`user_items`.`user_id`" -> 1),
                  "rhs" -> Map("`user_items`.`item_id`" -> 2)
                )
              ),
              "rhs" -> Map("`user_items`.`version`" -> 1)
            )
          )
        )
        val expected = Map(
          "db" -> db,
          "a" -> "b",
          "timestamp" -> date.getTime.asInstanceOf[AnyRef],
          "timing" -> 123L.asInstanceOf[AnyRef]
        )
        toJavaMap(expected)
      }

      getResult(update1).toString aka ("update1") must be equalTo (expected.toString)
      getResult(update2).toString aka ("update2") must be equalTo (expected.toString)
      getResult(update3).toString aka ("update3") must be equalTo (expected.toString)
    }
  }

}
