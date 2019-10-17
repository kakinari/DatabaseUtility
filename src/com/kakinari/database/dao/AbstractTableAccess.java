package com.kakinari.database.dao;

/*
 * TomcatのDBCP2を使ってDBをアクセスするための基本クラス。
 * このままでも使用可能だが、特定の接続ごとに派生クラスを作るほうが見やすくなる。
 * 結果はレコードごとにラベルと値のハッシュで返すようにしてあるので、ラベル名で各レコード参照のこと。
 * DBCP2を使用しているため、プログラム上は1クエリーごとにOpen-Closeしているが、接続の共有は
 * サーバレベルで行われている。　　ローカルスコープで必要なときにクラス生成し、廃棄してかまわない。
 * データの戻り値の設定のみをabstractとしている。
 */
/*
 *  使用する　Resource定義
 *  META-INF/context.xml に定義する。
 *  定義例
     <Resource name="${ConnectionName}"
      		auth="Container"
            type="javax.sql.DataSource"
		    factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
            username="${user}" password="${password}"
            driverClassName="${driverName}"
            url="jdbc:mysql://${hostName}:${port}/${databaseName}?${connectOption}"
            testWhileIdle="true"
            testOnBorrow="true"
            testOnReturn="false"
            validationQuery="SELECT 1"
			maxActive="100"
			minIdle="10"
			maxWait="10000"
			initialSize="10"
			removeAbandonedTimeout="60"
			removeAbandoned="true"
			logAbandoned="true"
			minEvictableIdleTimeMillis="30000"
			jmxEnabled="true"
			jdbcInterceptors="org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer"
		/>
 *
 *  ${ConnectionName}
 *  	このクラスで使用するコンストラクタで渡すID名
 *  ${DriverName}
 *  	MYSQL		:	com.mysql.cj.jdbc.Driver
 *  	SQLServer	:	com.microsoft.sqlserver.jdbc.SQLServerDrive
 *  		古いバージョンの SQLServerは JDBC 4.0はサポートしていないので、3.0のJDBC4ドライバーを使用すればOK.
 *  		それぞれのドライバはWEB-INF/libに配置すれば動作します。
 *  	testWhileIdolからvalidationIntervalまでのパラメータはプール時の再接続前に接続試験をあらかじめ行うためのパラメータ
 *  	放置されていたコネクションに接続するときにこれがないと、一度接続エラーが返るので設定しておいたほうが安全、
 *
 *  	CommitWorkなどトランザクションの操作を行いたい場合にはtypeをXADataSourceに変更する。
 *
 *  $databaseName は定義内に指定するとクエリー上では指定する必要がなくなるが、複数のデータベースで1つのコネクションを
 *  共有するためには定義でそれを省略しクエリーでテーブル指定の際にデータベースを指定することもできる。
 *
 * $connectOptionは、DB接続時に設定するオプションを記述する。
 * 　　例: useUnicode=false
 * 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public abstract class AbstractTableAccess {
	protected String connectionName = null;
	protected String databaseName   = null;
	private String sepa = "";

	/*
	 * Abstract関数リスト
	 */
	abstract protected Object getAccessResult(ResultSet rs) throws SQLException;

	protected AbstractTableAccess(String connectionName) {
		this.connectionName = connectionName;
	}

	protected void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	protected void setTypeMSSQL() {
		this.sepa = "";
	}

	protected void setTypeMysql() {
			this.sepa = "`";
	}

	protected String getShortName(String name) {
		return sepa + name + sepa;
	}

	protected String getTableName(String name) {
		return (databaseName.length()>0 ? getShortName(databaseName) + "." : "" ) + getShortName(name);
	}

	/*
	 * DBCP2を使用せず、直接JDBC経由で接続する方法
	 * Properties を使用してユーザや暗証を指定する。(nullを指定して、URLにパラメータとして指定してもOK)
	 * URLは接続用の文字列　"jdbc:mysql://HOST_NAME:PORT/YOUR_DBNAME"
	 */
	protected Connection openDirect(String driver, String url, Properties property) throws SQLException, ClassNotFoundException {
		Class.forName(driver);
		if (property != null)
			return DriverManager.getConnection(url, property);
		else
			return DriverManager.getConnection(url);
	}

	protected Connection open() throws NamingException, SQLException {
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		DataSource ds = (DataSource) envCtx.lookup("jdbc/" + connectionName);
		return ds.getConnection();
	}

	protected void close(Connection connect, Statement statement,ResultSet rs)  {
		if (rs != null) {
			try {
				if (! rs.isClosed())
					rs.close();
			} catch (SQLException e) {
			}
		}
		if (statement != null) {
			try {
				if (! statement.isClosed())
					statement.close();
			} catch (SQLException e) {
			}
		}
		if (connect != null) {
			try {
				if (! connect.isClosed())
					connect.close();
			} catch (SQLException e) {
			}
		}
	}

	public List<String> getLabels(ResultSet rs) throws SQLException {
		ArrayList<String> ret = new ArrayList<String>();
		if (rs == null) return ret;
		ResultSetMetaData columns;
		columns = rs.getMetaData();
		if (columns == null) return ret;

		for (int i = 1;i <= columns.getColumnCount(); i++) {  // rs は 1からのスタート
			ret.add(columns.getColumnLabel(i));
		}
		return ret;
	}

	public Object execute(String queryString) {
		ResultSet rs = null;
		Connection connect = null;
		Statement statement = null;
		if (queryString == null)
			return null;
		try {
			connect = open();
			statement = connect.createStatement();
			rs =  statement.executeQuery(queryString);
			return getAccessResult(rs);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close(connect, statement, rs);
		}
		return null;
	}

	public int executeUpdate(String queryString) {
		Connection connect = null;
		Statement statement = null;
		if (queryString == null)
			return -1;
		try {
			connect = open();
			statement = connect.createStatement();
			return statement.executeUpdate(queryString);
		} catch (SQLException | NamingException e) {
			e.printStackTrace();
		} finally {
			close(connect, statement, null);
		}
		return -1;
	}

	public static String getCondition(String colname, String[] data, String extra, String ordercond, String groupcond) {
		if (data == null)
			return "";
		String conn = " WHERE ";
		StringBuffer buff = new StringBuffer();
		if (extra != null) {
			buff.append(conn).append("(").append(extra).append(")");
			conn =  " AND (";
		}
		for (String row :data) {
			if (row == null || row.length() == 0)
				continue;
			if (row.contains("&")) {
				buff.append(conn);
				String conn2 = " (";
				for (String part : row.split("&")) {
					buff.append(conn2).append("`").append(colname).append("`").append(getCondition(part));
					conn2 = " AND ";
				}
				buff.append(")");
			} else {
				buff.append(conn).append("`").append(colname).append("`");
				buff.append(getCondition(row));
			}
		    conn = "\n OR ";
		}
		if (extra != null)
			buff.append(")");
		if (groupcond != null)
			buff.append("\n GROUP BY ").append(groupcond);
		if (ordercond != null)
			buff.append("\n ORDER BY ").append(ordercond);
		buff.append(";");
		return buff.toString();
	}

	private static Object getCondition(String row) {
		StringBuffer buff = new StringBuffer();
		if (row.contains("-")) {
			String[] keyval = row.split("-",2);
			buff.append(" BETWEEN '").append(keyval[0])
				  .append("' AND '").append(keyval[1]).append("'");
		} else if (row.contains(",")) {
			buff.append(" IN (");
			String head = "";
			for (String val : row.split(",")) {
				buff.append(head).append("'").append(val).append("'");
				head = ", ";
			}
			buff.append(")");
		} else if (row.startsWith("=")) {
			buff.append(" = '").append(row.substring(1)).append("'");
		} else if (row.startsWith("<>")) {
			buff.append(" <> '").append(row.substring(2)).append("'");
		} else if (row.startsWith("<")) {
			buff.append(" < '").append(row.substring(1)).append("'");
		} else if (row.startsWith(">")) {
			buff.append(" > '").append(row.substring(1)).append("'");
		} else if (row.equals("NULL")) {
			buff.append(" IS NULL");
		} else if (row.equals("!NULL")) {
			buff.append(" IS NOT NULL");
		} else if (row.startsWith("%")) {
			if (row.contains("_")) {
				buff.append(" LIKE '").append(row.replace("_", "$_")).append("' ESCAPE '$'");
			} else 
				buff.append(" LIKE '").append(row).append("'");
		} else {
			if (row.contains("_")) {
				buff.append(" LIKE '").append(row.replace("_", "$_")).append("%' ESCAPE '$'");
			} else 
				buff.append(" LIKE '").append(row).append("%'");
		}
		return buff.toString();
	}
}
