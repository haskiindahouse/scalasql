package scalasql.query

import scalasql.{Column, Queryable, ResultSetIterator, Table}
import scalasql.dialects.Dialect
import scalasql.renderer.SqlStr.{Renderable, SqlStringSyntax}
import scalasql.renderer.{Context, SqlStr}

trait InsertValues[Q, R] extends Query[Int] {
  def skipColumns(x: (Q => Column.ColumnExpr[_])*): InsertValues[Q, R]
}
object InsertValues {
  class Impl[Q, R](
      insert: Insert[Q, R],
      values: Seq[R],
      dialect: Dialect,
      qr: Queryable.Row[Q, R],
      skippedColumns: Seq[Column.ColumnExpr[_]]
  ) extends InsertValues[Q, R] {
    override protected def queryWalkExprs(): Seq[(List[String], Expr[_])] = Nil

    override protected def queryIsSingleRow: Boolean = true

    protected override def queryIsExecuteUpdate = true

    override protected def queryConstruct(args: ResultSetIterator): Int = args.get(dialect.IntType)

    override protected def renderToSql(ctx: Context): SqlStr = {
      new Renderer(
        Table.tableName(insert.table.value),
        Table.tableLabels(insert.table.value),
        values,
        ctx,
        qr,
        skippedColumns
      ).render()
    }

    override def skipColumns(x: (Q => Column.ColumnExpr[_])*) = {

      new Impl(
        insert,
        values,
        dialect,
        qr,
        skippedColumns ++ x.map(_(WithExpr.get(insert)))
      )
    }
  }
  class Renderer[Q, R](
      tableName: String,
      columnsList0: Seq[String],
      valuesList: Seq[R],
      ctx: Context,
      qr: Queryable.Row[Q, R],
      skippedColumns: Seq[Column.ColumnExpr[_]]
  ) {

    lazy val skippedColumnsNames = skippedColumns.map(_.name).toSet

    lazy val (liveCols, liveIndices) = columnsList0.zipWithIndex.filter { case (c, i) =>
      !skippedColumnsNames.contains(c)
    }.unzip

    lazy val columns = SqlStr.join(
      liveCols.map(s => SqlStr.raw(ctx.config.columnNameMapper(s))),
      SqlStr.commaSep
    )

    lazy val liveIndicesSet = liveIndices.toSet

    val valuesSqls = valuesList.map { v =>
      val commaSeparated = SqlStr.join(
        qr.deconstruct(v)
          .zipWithIndex
          .collect { case (s, i) if liveIndicesSet.contains(i) => sql"$s" },
        SqlStr.commaSep
      )
      sql"(" + commaSeparated + sql")"
    }

    lazy val values = SqlStr.join(valuesSqls, SqlStr.commaSep)

    def render() = {
      sql"INSERT INTO ${SqlStr.raw(ctx.config.tableNameMapper(tableName))} ($columns) VALUES $values"
    }
  }
}