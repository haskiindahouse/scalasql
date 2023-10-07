package usql

import usql.SqlStr.SqlStringSyntax

object ExprOps extends ExprOps
trait ExprOps {
  implicit class ExprIntOps0(v: Expr[Int]) {
    def *(x: Int): Expr[Int] = Expr { implicit ctx => v.toSqlExpr + usql" * $x" }
    def >(x: Int): Expr[Boolean] = Expr { implicit ctx => v.toSqlExpr + usql" > $x" }
  }
  implicit class ExprOps0(v: Expr[_]) {
    def ===(x: Int): Expr[Boolean] = Expr { implicit ctx => v.toSqlExpr + usql" = $x" }
    def ===(x: String): Expr[Boolean] = Expr { implicit ctx => v.toSqlExpr + usql" = $x" }
    def ===(x: Expr[_]): Expr[Boolean] = Expr{ implicit ctx => v.toSqlExpr + usql" = " + x.toSqlExpr }
  }
  implicit class ExprBooleanOps0(v: Expr[Boolean]) {
    def &&(x: Expr[Boolean]): Expr[Boolean] = Expr { implicit ctx => v.toSqlExpr + usql" AND " + x.toSqlExpr }
  }
}