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
    def writes(nodes: Seq[A]) = {
      val ary = new util.ArrayList[Fluent]()
      nodes.foreach { node =>
        ary.add(writes.writes(node))
      }
      ary
    }
  }

  implicit object SqlAggWrites extends JavaMapWrites[SqlAgg] {
    import format.agg._
    def writes(a: SqlAgg): DefaultWrites.Fluent = a match {
      case ac: AggCall =>
        implicitly[JavaMapWrites[AggCall]].writes(ac)
      case a: Avg =>
        implicitly[JavaMapWrites[Avg]].writes(a)
      case ce: CountExpr =>
        implicitly[JavaMapWrites[CountExpr]].writes(ce)
      case cs: CountStar =>
        implicitly[JavaMapWrites[CountStar]].writes(cs)
      case gc: GroupConcat =>
        implicitly[JavaMapWrites[GroupConcat]].writes(gc)
      case m: Max =>
        implicitly[JavaMapWrites[Max]].writes(m)
      case m: Min =>
        implicitly[JavaMapWrites[Min]].writes(m)
      case s: Sum =>
        implicitly[JavaMapWrites[Sum]].writes(s)
    }
  }

  implicit object SqlFunctionWrites extends JavaMapWrites[SqlFunction] {
    import format.function._
    def writes(a: SqlFunction) = {
      a match {
        case e: Extract =>
          implicitly[JavaMapWrites[Extract]].writes(e)
        case fc: FunctionCall =>
          implicitly[JavaMapWrites[FunctionCall]].writes(fc)
        case s: Substring =>
          implicitly[JavaMapWrites[Substring]].writes(s)
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

  implicit object SqlExprWrites extends JavaMapWrites[SqlExpr] {
    import format.expr._
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
        case fi: FieldIdent =>
          implicitly[JavaMapWrites[FieldIdent]].writes(fi)
        case ss: Subselect =>
          implicitly[JavaMapWrites[Subselect]].writes(ss)
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
                proj.put("expression", expr.sql)
                alias foreach { a =>
                  proj.put("alias", a)
                }
              case StarProj(_) =>
                proj.put("expression", "*")
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
