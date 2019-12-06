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
 * 
 * 上記のコメントはDBCP2用のものですが、接続用のプロパティ設定の参考にしてください。
 */

/*
 * DBCP2を使用せず、直接JDBC経由で接続する方法
 * Properties を使用してユーザや暗証を指定する。(nullを指定して、URLにパラメータとして指定してもOK)
 * URLは接続用の文字列　"jdbc:mysql://HOST_NAME:PORT/YOUR_DBNAME"
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

public abstract class AbstractDirectTableAccess {
	private String driverName = null;
	private String URL   = null;
	private Properties property = null;
	private String KEY_USER = "username";
	private String KEY_PASSWORD ="password";
	/*
	 * Abstract関数リスト
	 */
	abstract protected Object getAccessResult(ResultSet rs) throws SQLException;

	protected AbstractDirectTableAccess(String driver, String url) {
		this.driverName = driver;
		this.URL = url;
	}

	protected AbstractDirectTableAccess(String driver, String url, Properties property) {
		this.driverName = driver;
		this.URL = url;
		this.property = property;
	}

	public void setUser(String user) {
		if (this.property == null)
			this.property = new Properties();
		this.property.put(KEY_USER, user);
	}
	
	public void setPassword(String secret) {
		if (this.property == null)
			this.property = new Properties();
		this.property.put(KEY_PASSWORD, secret);
	}

	public void setProperty(String key, String value) {
		if (this.property == null)
			this.property = new Properties();
		this.property.put(key, value);
	}
	
	protected Connection open() throws SQLException, ClassNotFoundException {
		try {
			Class.forName(this.driverName).newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		if (this.property != null)
			return DriverManager.getConnection(this.URL, this.property);
		else
			return DriverManager.getConnection(this.URL);
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
		try {
			connect = open();
			statement = connect.createStatement();
			rs =  statement.executeQuery(queryString);
			return getAccessResult(rs);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			close(connect, statement, rs);
		}
		return null;
	}

	public int executeUpdate(String queryString) {
		Connection connect = null;
		Statement statement = null;
		try {
			connect = open();
			statement = connect.createStatement();
			return statement.executeUpdate(queryString);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			close(connect, statement, null);
		}
		return -1;
	}
}
