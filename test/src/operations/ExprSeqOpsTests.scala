package usql.operations

import usql._
import utest._
import ExprOps._

object SqliteExprSeqOpsTests extends ExprSeqOpsTests with SqliteSuite
object PostgresExprExprSeqOpsTests extends ExprSeqOpsTests with PostgresSuite
object MySqlExprExprSeqOpsTests extends ExprSeqOpsTests with MySqlSuite

/**
 * Tests for all the aggregate operators that we provide by default
 */
trait ExprSeqOpsTests extends TestSuite {
  val checker: TestDb
  def tests = Tests {
    test("size") - checker(
      query = Purchase.select.size,
      sql = "SELECT COUNT(1) as res FROM purchase purchase0",
      value = 7
    )

    test("sumBy") - checker(
      query = Purchase.select.sumBy(_.count),
      sql = "SELECT SUM(purchase0.count) as res FROM purchase purchase0",
      value = 140
    )

    test("minBy") - checker(
      query = Purchase.select.minBy(_.count),
      sql = "SELECT MIN(purchase0.count) as res FROM purchase purchase0",
      value = 3
    )

    test("maxBy") - checker(
      query = Purchase.select.maxBy(_.count),
      sql = "SELECT MAX(purchase0.count) as res FROM purchase purchase0",
      value = 100
    )

    test("avgBy") - checker(
      query = Purchase.select.avgBy(_.count),
      sql = "SELECT AVG(purchase0.count) as res FROM purchase purchase0",
      value = 20
    )
  }
}
