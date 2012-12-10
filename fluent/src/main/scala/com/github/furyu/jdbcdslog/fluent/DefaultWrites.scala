package com.github.furyu.jdbcdslog.fluent

import com.github.stephentu.scalasqlparser._

import java.util

object DefaultWrites {
  trait Writes[A,B] {
    def writes(a: A): B
  }

  type Fluent = AnyRef

  trait JavaMapWrites[A] extends Writes[A, Fluent]{
    def writes(a: A): Fluent
  }

  implicit object AddExprWrites extends JavaMapWrites[AddExpr] {
    def writes(node: AddExpr) = {
      val fluent = new util.HashMap[String, AnyRef]()
      node match {
        case node: AddExpr =>
          val data = new util.HashMap[String, AnyRef]()
          data.put("lhs", SqlExprWrites.writes(node.lhs))
          data.put("rhs", SqlExprWrites.writes(node.rhs))
          fluent.put("add", data)
      }
      // implicitly[Writes[AddExpr, util.Map[String, AnyRef]].writes(addExpr)
      fluent
    }

  }

  implicit object BinopWrites extends JavaMapWrites[Binop] {
    def writes(binop: Binop) = {
      binop match {
        case addExpr: AddExpr =>
          implicitly[JavaMapWrites[AddExpr]].writes(addExpr)
      }
    }
  }

  class SeqWrites[A](implicit writes: JavaMapWrites[A]) extends JavaMapWrites[Seq[A]] {
    import DefaultWrites.{CaseExprCaseWrites}
    def writes(nodes: Seq[A]) = {
      val ary = new util.ArrayList[AnyRef]()
      nodes.foreach { node =>
        ary.add(writes.writes(node))
      }
      ary
    }
  }

  implicit object CaseExprCaseWrites extends JavaMapWrites[CaseExprCase] {
    def writes(a: CaseExprCase) = {
      val CaseExprCase(cond, expr, _) = a
      val data = new util.HashMap[String, AnyRef]()
      data.put("cond", SqlExprWrites.writes(cond))
      data.put("expr", SqlExprWrites.writes(expr))
      data
    }
  }

  implicit object SeqCaseExprCaseWrites extends SeqWrites[CaseExprCase]

  implicit object CaseExprWrites extends JavaMapWrites[CaseExpr] {
    def writes(a: CaseExpr) = {
      val data = new util.HashMap[String, AnyRef]()
      val CaseExpr(expr, cases, default, _) = a
      data.put("expr", SqlExprWrites.writes(expr))
      data.put("cases", implicitly[JavaMapWrites[Seq[CaseExprCase]]].writes(cases))
      data
    }
  }

  implicit object CaseWhenExprWrites extends JavaMapWrites[CaseWhenExpr] {
    def writes(a: CaseWhenExpr): DefaultWrites.Fluent = {
      val data = new util.HashMap[String, AnyRef]()
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
    def writes(a: LiteralExpr) = {
      a match {
        case DateLiteral(d, _) =>
          // TODO
          null
        case FloatLiteral(v, _) =>
          // TODO
          null
        case IntLiteral(i, _) =>
          // TODO
          null
        case IntervalLiteral(e, unit, _) =>
          // TODO
          null
        case NullLiteral(_) =>
          // TODO
          null
        case StringLiteral(v, _) =>
          // TODO
          null
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

  implicit object SqlExprWrites extends JavaMapWrites[SqlExpr] {
    def writes(sqlExpr: SqlExpr) = {
      sqlExpr match {
        case binop: Binop =>
          implicitly[JavaMapWrites[Binop]].writes(binop)
        case caseExpr: CaseExpr =>
          implicitly[JavaMapWrites[CaseExpr]].writes(caseExpr)
        case caseWhenExpr: CaseWhenExpr =>
          implicitly[JavaMapWrites[CaseWhenExpr]].writes(caseWhenExpr)
        case agg: SqlAgg =>
          implicitly[JavaMapWrites[SqlAgg]].writes(agg)
        case fun: SqlFunction =>
          implicitly[JavaMapWrites[SqlFunction]].writes(fun)
        case lit: LiteralExpr =>
          implicitly[JavaMapWrites[LiteralExpr]].writes(lit)
        case ex: Exists =>
          implicitly[JavaMapWrites[Exists]].writes(ex)
        case in: In =>
          implicitly[JavaMapWrites[In]].writes(in)
        case un: Unop =>
          implicitly[JavaMapWrites[Unop]].writes(un)
      }
    }
  }

  implicit object NodeWrites extends JavaMapWrites[Node] {
    def writes(node: Node) = {
      node match {
        case e: SqlExpr =>
          implicitly[JavaMapWrites[SqlExpr]].writes(e)
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
      val data = new util.HashMap[String, AnyRef]()
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
      val data = new util.HashMap[String, AnyRef]()
      data.put("select", StmtWrites.writes(select))
      data.put("alias", alias)
      data
    }
  }

  implicit object TableRelationASTWrites extends JavaMapWrites[TableRelationAST] {
    def writes(a: TableRelationAST) = {
      val TableRelationAST(name, alias, _) = a
      val data = new util.HashMap[String, AnyRef]()
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
    def consumeRelations(db: util.Map[String, AnyRef], relations: Seq[SqlRelation]) {
      val rels = new util.ArrayList[AnyRef]()
      relations.foreach {
        case TableRelationAST(name, alias, _) =>
          val rel = new util.HashMap[String, AnyRef]()
          rel.put("table", name)
          alias.foreach { alias =>
            rel.put("alias", alias)
          }
          rels.add(rel)
      }
      db.put("relations", rels)
    }
    def consumeWhereClause(db: util.Map[String, AnyRef], filter: Option[SqlExpr]) {
      val where = new util.HashMap[String, AnyRef]()
      db.put("where", where)
      filter.foreach {
        case Eq(lhs, rhs, _) =>
          where.put(lhs.sql, rhs.sql)
      }
    }

    def writes(n: Stmt) = {
      val db = new util.HashMap[String, AnyRef]()
      n match {
        case SelectStmt(projections, relations, filter, groupBy, orderBy, limit, _) =>
          db.put("command", "select")
          db.put("timestamp", new java.util.Date().getTime().asInstanceOf[AnyRef])

          val projs = new util.ArrayList[util.Map[String, AnyRef]]()
          projections.foreach { p =>
            val proj = new util.HashMap[String, AnyRef]()
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
          db.put("timestamp", new java.util.Date().getTime.asInstanceOf[AnyRef])

          db.put("table", tableName)
          val params = new util.HashMap[String, AnyRef]()
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
          db.put("timestamp", new java.util.Date().getTime().asInstanceOf[AnyRef])

          consumeRelations(db, relations)
          consumeWhereClause(db, filter)

          val as = new util.HashMap[String, AnyRef]()
          db.put("assigns", as)
          assigns.foreach { case Assign(lhs, rhs, _) =>
            as.put(lhs.sql, rhs.sql)
          }
      }
      db
    }
  }
}
