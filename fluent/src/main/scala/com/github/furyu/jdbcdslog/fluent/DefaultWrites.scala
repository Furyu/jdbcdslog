package com.github.furyu.jdbcdslog.fluent

import com.github.stephentu.scalasqlparser._

import java.util

object DefaultWrites {
  trait Writes[A,B] {
    def writes(a: A): B
  }

  type Fluent = Any

  trait JavaMapWrites[A] extends Writes[A, Fluent]{
    def writes(a: A): Fluent
  }

  implicit object BinopWrites extends JavaMapWrites[Binop] {
    import format.binop._
    def writes(binop: Binop) = {
      binop match {
        case eq: Eq =>
          implicitly[JavaMapWrites[Eq]].writes(eq)
        case neq: Neq =>
          implicitly[JavaMapWrites[Neq]].writes(neq)
        case addExpr: AddExpr =>
          implicitly[JavaMapWrites[AddExpr]].writes(addExpr)
        case mul: Mult =>
          implicitly[JavaMapWrites[Mult]].writes(mul)
        case div: Div =>
          implicitly[JavaMapWrites[Div]].writes(div)
        case and: And =>
          implicitly[JavaMapWrites[And]].writes(and)
        case or: Or =>
          implicitly[JavaMapWrites[Or]].writes(or)
        case like: Like =>
          implicitly[JavaMapWrites[Like]].writes(like)
        case ineqLike: InequalityLike =>
          implicitly[JavaMapWrites[InequalityLike]].writes(ineqLike)
      }
    }
  }

  class SeqWrites[A](implicit writes: JavaMapWrites[A]) extends JavaMapWrites[Seq[A]] {
    import DefaultWrites.{CaseExprCaseWrites}
    def writes(nodes: Seq[A]) = {
      val ary = new util.ArrayList[Fluent]()
      nodes.foreach { node =>
        ary.add(writes.writes(node))
      }
      ary
    }
  }

  implicit object CaseExprCaseWrites extends JavaMapWrites[CaseExprCase] {
    def writes(a: CaseExprCase) = {
      val CaseExprCase(cond, expr, _) = a
      val data = new util.HashMap[String, Fluent]()
      data.put("cond", SqlExprWrites.writes(cond))
      data.put("expr", SqlExprWrites.writes(expr))
      data
    }
  }

  implicit object SeqCaseExprCaseWrites extends SeqWrites[CaseExprCase]

  implicit object CaseExprWrites extends JavaMapWrites[CaseExpr] {
    def writes(a: CaseExpr) = {
      val data = new util.HashMap[String, Fluent]()
      val CaseExpr(expr, cases, default, _) = a
      data.put("expr", SqlExprWrites.writes(expr))
      data.put("cases", implicitly[JavaMapWrites[Seq[CaseExprCase]]].writes(cases))
      data
    }
  }

  implicit object CaseWhenExprWrites extends JavaMapWrites[CaseWhenExpr] {
    def writes(a: CaseWhenExpr): DefaultWrites.Fluent = {
      val data = new util.HashMap[String, Fluent]()
      val CaseWhenExpr(cases, expr, _) = a

      data.put("cases", implicitly[JavaMapWrites[Seq[CaseExprCase]]].writes(cases))
      expr.foreach { e =>
        data.put("expr", SqlExprWrites.writes(e))
      }
      data
    }
  }

  // sql expr writes

  implicit object SqlAggWrites extends JavaMapWrites[SqlAgg] {
    def writes(a: SqlAgg)= {
      // TODO
      // AggCall
      // Avg
      // CountExpr
      // CountStart
      // GroupConcat
      // Max
      // Min
      // Sum
      null
    }
  }

  implicit object SqlFunctionWrites extends JavaMapWrites[SqlFunction] {
    def writes(a: SqlFunction) = {
      a match {
        case Extract(expr, what, _) =>
          // TODO
          null
        case FunctionCall(name, args, _) =>
          // TODO
          null
        case Substring(expr, from, length, _) =>
          // TODO
          null
      }
    }
  }

  implicit object LiteralExprWrites extends JavaMapWrites[LiteralExpr] {
    import format.literal._
    def writes(a: LiteralExpr) = {
      a match {
        case d: DateLiteral =>
          implicitly[JavaMapWrites[DateLiteral]].writes(d)
        case f: FloatLiteral =>
          implicitly[JavaMapWrites[FloatLiteral]].writes(f)
        case i: IntLiteral =>
          implicitly[JavaMapWrites[IntLiteral]].writes(i)
        case i: IntervalLiteral =>
          implicitly[JavaMapWrites[IntervalLiteral]].writes(i)
        case n: NullLiteral =>
          implicitly[JavaMapWrites[NullLiteral]].writes(n)
        case s: StringLiteral =>
          implicitly[JavaMapWrites[StringLiteral]].writes(s)
      }
    }
  }

  implicit object ExistsWrites extends JavaMapWrites[Exists] {
    def writes(a: Exists) = {
      val Exists(Subselect(select, _), _) = a
      // TODO
      null
    }
  }

  implicit object InWrites extends JavaMapWrites[In] {
    def writes(a: In) = {
      // TODO
      null
    }
  }

  implicit object Unopwrites extends JavaMapWrites[Unop] {
    def writes(unop: Unop) = {
      // TODO
      null
    }
  }

  implicit object ColumnSymbolWrites extends JavaMapWrites[ColumnSymbol] {
    def writes(a: ColumnSymbol) = {
      val ColumnSymbol(relation, column, _) = a
      val data = new util.HashMap[String, Any]()
      data.put("relation", relation)
      data.put("column", column)
      data
    }
  }

  implicit object ProjectionSymbolWrites extends JavaMapWrites[ProjectionSymbol] {
    def writes(a: ProjectionSymbol) = {
      val ProjectionSymbol(name, _) = a
      val data = new util.HashMap[String, Any]()
      data.put("name", name)
      data
    }
  }

  implicit object SymbolWrites extends JavaMapWrites[com.github.stephentu.scalasqlparser.Symbol] {
    def writes(a: Symbol) = {
      a match {
        case c: ColumnSymbol =>
          implicitly[JavaMapWrites[ColumnSymbol]].writes(c)
        case p: ProjectionSymbol =>
          implicitly[JavaMapWrites[ProjectionSymbol]].writes(p)
        case unexpected =>
          println("Unexpected: " + unexpected)
          null
      }
    }
  }

  implicit object FieldIdentWrites extends JavaMapWrites[FieldIdent] {
    def writes(fi: FieldIdent) = {
      val FieldIdent(qualifier, name, symbol, _) = fi
      val data = new util.HashMap[String,Any]()
      qualifier.foreach { q =>
        data.put("qualifier", q)
      }
      data.put("name", name)
      data.put("symbol", SymbolWrites.writes(symbol))
      data
    }
  }

  implicit object SqlExprWrites extends JavaMapWrites[SqlExpr] {
    def writes(sqlExpr: SqlExpr) = {
      sqlExpr match {
        case binop: Binop =>
          implicitly[JavaMapWrites[Binop]].writes(binop)
        case caseExpr: CaseExpr =>
//          implicitly[JavaMapWrites[CaseExpr]].writes(caseExpr)
          // TODO
          null
        case caseWhenExpr: CaseWhenExpr =>
//          implicitly[JavaMapWrites[CaseWhenExpr]].writes(caseWhenExpr)
          // TODO
          null
        case agg: SqlAgg =>
//          implicitly[JavaMapWrites[SqlAgg]].writes(agg)
          // TODO
          null
        case fun: SqlFunction =>
//          implicitly[JavaMapWrites[SqlFunction]].writes(fun)
          // TODO
          null
        case lit: LiteralExpr =>
          implicitly[JavaMapWrites[LiteralExpr]].writes(lit)
        case ex: Exists =>
//          implicitly[JavaMapWrites[Exists]].writes(ex)
          // TODO
          null
        case in: In =>
//          implicitly[JavaMapWrites[In]].writes(in)
          // TODO
          null
        case un: Unop =>
//          implicitly[JavaMapWrites[Unop]].writes(un)
          // TODO
          null
        case fi: FieldIdent =>
          implicitly[JavaMapWrites[FieldIdent]].writes(fi)
        case ss: Subselect =>
//          implicitly[JavaMapWrites[Subselect]].writes(ss)
          // TODO
          null
      }
    }
  }

  implicit object JoinTypeWrites extends JavaMapWrites[JoinType] {
    def writes(a: JoinType): DefaultWrites.Fluent = {
      a.sql
    }
  }

  implicit object JoinRelationWrites extends JavaMapWrites[JoinRelation] {
    def writes(a: JoinRelation) = {
      val JoinRelation(left, right, tpe, clause, _) = a
      val data = new util.HashMap[String, Fluent]()
      data.put("left", SqlRelationWrites.writes(left))
      data.put("right", SqlRelationWrites.writes(right))
      data.put("type", JoinTypeWrites.writes(tpe))
      data.put("clause", SqlExprWrites.writes(clause))
      data
    }
  }

  implicit object SubqueryRelationASTWrites extends JavaMapWrites[SubqueryRelationAST] {
    def writes(s: SubqueryRelationAST) = {
      val SubqueryRelationAST(select, alias, _) = s
      val data = new util.HashMap[String, Fluent]()
      data.put("select", StmtWrites.writes(select))
      data.put("alias", alias)
      data
    }
  }

  implicit object TableRelationASTWrites extends JavaMapWrites[TableRelationAST] {
    def writes(a: TableRelationAST) = {
      val TableRelationAST(name, alias, _) = a
      val data = new util.HashMap[String, Fluent]()
      data.put("name", name)
      alias.foreach { a =>
        data.put("alias", a)
      }
      data
    }
  }

  implicit object SqlRelationWrites extends JavaMapWrites[SqlRelation] {
    def writes(a: SqlRelation) = {
      a match {
        case j: JoinRelation =>
          implicitly[JavaMapWrites[JoinRelation]].writes(j)
        case s: SubqueryRelationAST =>
          implicitly[JavaMapWrites[SubqueryRelationAST]].writes(s)
        case t: TableRelationAST =>
          implicitly[JavaMapWrites[TableRelationAST]].writes(t)
      }
    }
  }

  implicit object StmtWrites extends JavaMapWrites[Stmt] {
    def consumeRelations(db: util.Map[String, Fluent], relations: Seq[SqlRelation]) {
      val rels = new util.ArrayList[Fluent]()
      relations.foreach {
        case rel =>
          rels.add(implicitly[JavaMapWrites[SqlRelation]].writes(rel))
      }
      db.put("relations", rels)
    }
    def consumeWhereClause(db: util.Map[String, Fluent], filter: Option[SqlExpr]) {
      val where = new util.ArrayList[Fluent]()
      db.put("where", where)
      filter.foreach { expr =>
          where.add(implicitly[JavaMapWrites[SqlExpr]].writes(expr))
      }
    }

    def writes(n: Stmt) = {
      val db = new util.HashMap[String, Fluent]()
      n match {
        case SelectStmt(projections, relations, filter, groupBy, orderBy, limit, _) =>
          db.put("command", "select")
          db.put("timestamp", new java.util.Date().getTime().asInstanceOf[Fluent])

          val projs = new util.ArrayList[util.Map[String, Fluent]]()
          projections.foreach { p =>
            val proj = new util.HashMap[String, Fluent]()
            projs.add(proj)
            p match {
              case ExprProj(expr, alias, _) =>
                proj.put("expr", expr.sql)
                alias foreach { a =>
                  proj.put("alias", a)
                }
              case StarProj(_) =>
                proj.put("expr", "*")
            }
          }
          db.put("projections", projs)
          relations foreach { r =>
            consumeRelations(db, r)
          }
          consumeWhereClause(db, filter)
        case InsertStmt(tableName, insRow, _) =>
          db.put("command", "insert")
          db.put("timestamp", new java.util.Date().getTime.asInstanceOf[Fluent])

          db.put("table", tableName)
          val params = new util.HashMap[String, Fluent]()
          db.put("assigns", params)
          insRow match {
            case com.github.stephentu.scalasqlparser.Set(assigns, _) =>
              assigns.foreach { assign =>
                params.put(assign.lhs.sql, assign.rhs.sql)
              }
            case com.github.stephentu.scalasqlparser.Values(values, _) =>
              values.foreach { value =>
                params.put("colNameFor" + value, value.sql)
              }
          }
        case UpdateStmt(relations, assigns, filter, _) =>
          db.put("command", "update")
          db.put("timestamp", new java.util.Date().getTime().asInstanceOf[Fluent])

          consumeRelations(db, relations)
          consumeWhereClause(db, filter)

          val as = new util.HashMap[String, Fluent]()
          db.put("assigns", as)
          assigns.foreach { case Assign(lhs, rhs, _) =>
            as.put(lhs.sql, rhs.sql)
          }
      }
      db
    }
  }
}
