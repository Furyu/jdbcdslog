package jp.furyu.jdbcdslog.fluent.format

import jp.furyu.jdbcdslog.fluent.DefaultWrites.JavaMapWrites
import com.github.stephentu.scalasqlparser.{TableRelation, ColumnSymbol, FieldIdent}

object field_ident {

  implicit object map extends JavaMapWrites[FieldIdent] {
    def writes(fi: FieldIdent) = {
      val FieldIdent(explicitQualifier, name, symbol, _) = fi
      val relationName = Option(symbol).collect {
        case ColumnSymbol(rel, col, _) =>
          rel
      }
      val qualifier = explicitQualifier.orElse(relationName)
      val tableName = for {
        q <- qualifier
        ctx <- Option(fi.ctx)
        n <- ctx.relations.collectFirst {
          case (relName, TableRelation(tblName)) if relName == q =>
            tblName
        }
      } yield n
      val field = tableName.orElse(qualifier).map("`" + _ + "`.").getOrElse("") + "`" + name + "`"
      expression("field", field)
    }
  }

  implicit object str extends JavaMapWrites[FieldIdent] {
    def writes(fi: FieldIdent) = {
      val FieldIdent(explicitQualifier, name, symbol, _) = fi
      val relationName = Option(symbol).collect {
        case ColumnSymbol(rel, col, _) =>
          rel
      }
      val qualifier = explicitQualifier.orElse(relationName)
      val tableName = for {
        q <- qualifier
        ctx <- Option(fi.ctx)
        n <- ctx.relations.collectFirst {
          case (relName, TableRelation(tblName)) if relName == q =>
            tblName
        }
      } yield n
      val field = tableName.orElse(qualifier).map("`" + _ + "`.").getOrElse("") + "`" + name + "`"
      field
    }
  }

}
