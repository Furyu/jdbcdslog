package jp.furyu.jdbcdslog.fluent

import org.specs2.mutable._
import com.github.stephentu.scalasqlparser.{IntType, TableColumn, Definitions}

class FluentMarshallerSpec extends Specification {

  "FluentMarshaller" should {
    "marshal SQL statements" in {
      val marshaller = new FluentMarshaller
      val definitions = new Definitions(
        Map("items" -> Seq(
          TableColumn("id", IntType(8)),
          TableColumn("value", IntType(8))
        )))
      val dateString = "2012/12/28 13:08:00"
      val dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
      val date = dateFormat.parse(dateString)
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
          "relations" -> Array(
            Map(
              "name" -> "items"
            )
          ),
          "where" -> Array(
            Map(
              "eq" -> Map(
                "rhs" -> 1.asInstanceOf[AnyRef],
                "lhs" -> Map(
                  "field" -> "`items`.`id`"
                )
              )
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
      result.toString must be equalTo (expected.toString)
    }
  }

}
