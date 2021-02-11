package baggagedemo.db;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import aeonics.any.Any;
import aeonics.server.http.URLDecoder;
import oracle.nosql.driver.NoSQLException;
import oracle.nosql.driver.NoSQLHandle;
import oracle.nosql.driver.NoSQLHandleConfig;
import oracle.nosql.driver.NoSQLHandleFactory;
import oracle.nosql.driver.iam.SignatureProvider;
import oracle.nosql.driver.ops.PrepareRequest;
import oracle.nosql.driver.ops.QueryRequest;
import oracle.nosql.driver.ops.QueryResult;
import oracle.nosql.driver.values.*;

/**
 * <h1>Connection String</h1>
 * <p>The connection string to use is:</p>
 * <pre>
 *  jdbc:nosql://[host]:[port]/[path]
 *  	?tenantId=
 *  	&userId=
 *  	&fingerprint=
 *  	&privateKey=
 *  	&passPhrase=
 *  	&compartment=
 * </pre>
 * 
 * <h1>Rationale</h1>
 * <p>We want to be able to reuse the SQL connection pooling and thread pool handling
 * and thus we need to encapsulate the NoSQL in a compatible java.sql structure.</p>
 * <p>Only currently used methods are implemented, so this is not a generic compliant proxy.</p>
 * <p>Note that even though we are using PreparedStatement we do not truly reuse them. This
 * is motivated by the need for concurrency, connection pooling to possibly different instances
 * and connection isolation. If the database it too slow (advantage of PreparedStatement) then rather
 * connect to more databases or have more parallel connections rather than limit throughput by enforcing
 * any sort of synchronization at the endpoint level. Oracle NoSQL has a PreparedStatement.copyStatement() 
 * that "should" mitigate this issue but there is nothing alike in the standard java.sql so the internal 
 * database pooling implementation cannot leverage this mechanism.</p>
 */
public class Database
{
	private static _Driver driver = new _Driver();
	public static void register()
	{
		try
		{
			DriverManager.registerDriver(driver);
		}
		catch(SQLException e)
		{
			aeonics.bootstrap.Logger.log(aeonics.bootstrap.Logger.SEVERE, Database.class, e);
		}
	}
	
	public static void unregister()
	{
		try
		{
			DriverManager.deregisterDriver(driver);
		}
		catch(SQLException e)
		{
			aeonics.bootstrap.Logger.log(aeonics.bootstrap.Logger.SEVERE, Database.class, e);
		}
	}
	
	private static class _Driver implements Driver
	{
		public Connection connect(String url, Properties info) throws SQLException
		{
			if( url == null || !url.startsWith("jdbc:nosql://") ) return null;
			
			try
			{
				URL u = new URL("https" + url.substring(10));
				String endpoint = "https://" + u.getHost() + (u.getPort()==-1?"":":"+u.getPort()) + u.getPath();
				Any params = Any.wrap(URLDecoder.decode(u.getQuery()));
				
				return new _Connection(endpoint, 
						params.asString("tenantId"), 
						params.asString("userId"),
						params.asString("fingerprint"),
						params.asString("privateKey"), 
						params.asString("passPhrase"), 
						params.asString("compartment")
					);
			}
			catch(Exception e)
			{
				throw new SQLException(e);
			}
		}

		public boolean acceptsURL(String url) throws SQLException
		{
			return url != null && url.startsWith("jdbc:nosql://");
		}

		public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
			return new DriverPropertyInfo[]
			{
				new DriverPropertyInfo("endpoint", null),
				new DriverPropertyInfo("tenantId", null),
				new DriverPropertyInfo("userId", null),
				new DriverPropertyInfo("fingerprint", null),
				new DriverPropertyInfo("privateKey", null),
				new DriverPropertyInfo("passPhrase", null),
				new DriverPropertyInfo("compartment", null)
			};
		}

		public int getMajorVersion() {return 0;}
		public int getMinorVersion() {return 0;}
		public boolean jdbcCompliant() {return false;}
		public Logger getParentLogger() throws SQLFeatureNotSupportedException {throw new SQLFeatureNotSupportedException();}
	}
	
	private static class _Connection implements Connection
	{
		private NoSQLHandle connection = null;
		
		public _Connection(String endpoint, String tenantId, String userId, String fingerprint, String privateKey, String passPhrase, String compartment)
		{
			NoSQLHandleConfig config = new NoSQLHandleConfig(endpoint);
            config.setConnectionPoolSize(1);
            SignatureProvider authProvider = new SignatureProvider(tenantId, userId, fingerprint, privateKey, passPhrase.toCharArray());
            config.setRequestTimeout(15000);
            config.setAuthorizationProvider(authProvider);
            config.configureDefaultRetryHandler(1, 10);
            config.setDefaultCompartment(compartment);
			connection = NoSQLHandleFactory.createNoSQLHandle(config);
		}
		
		public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
		{
			if( connection == null ) throw new SQLException("Connection closed");
			
			PrepareRequest request = new PrepareRequest().setStatement(sql);
			request.setGetQueryPlan(true);
            oracle.nosql.driver.ops.PreparedStatement prepared = connection.prepare(request).getPreparedStatement();
            
			return new _PreparedStatement(prepared, connection);
		}
		
		public boolean isValid(int timeout) throws SQLException
		{
			// only called in case of sql exception to destroy the connection
			// so always tell we are invalid so we get closed properly
			return false;
		}
		
		public void close() throws SQLException
		{
			connection.close();
			connection = null;
		}
		
		public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
		public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}
		public CallableStatement prepareCall(String sql) throws SQLException {return null;}
		public String nativeSQL(String sql) throws SQLException {return null;}
		public void setAutoCommit(boolean autoCommit) throws SQLException {}
		public boolean getAutoCommit() throws SQLException {return false;}
		public void commit() throws SQLException {}
		public void rollback() throws SQLException {}
		public void setReadOnly(boolean readOnly) throws SQLException {}
		public boolean isReadOnly() throws SQLException {return false;}
		public void setCatalog(String catalog) throws SQLException {}
		public String getCatalog() throws SQLException {return null;}
		public void setTransactionIsolation(int level) throws SQLException {}
		public int getTransactionIsolation() throws SQLException {return 0;}
		public SQLWarning getWarnings() throws SQLException {return null;}
		public void clearWarnings() throws SQLException {}
		public boolean isClosed() throws SQLException {return false;}
		public Statement createStatement() throws SQLException {return null;}
		public PreparedStatement prepareStatement(String sql) throws SQLException {return null;}
		public DatabaseMetaData getMetaData() throws SQLException {return null;}
		public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {return null;}
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {return null;}
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {return null;}
		public Map<String, Class<?>> getTypeMap() throws SQLException {return null;}
		public void setTypeMap(Map<String, Class<?>> map) throws SQLException {}
		public void setHoldability(int holdability) throws SQLException {}
		public int getHoldability() throws SQLException {return 0;}
		public Savepoint setSavepoint() throws SQLException {return null;}
		public Savepoint setSavepoint(String name) throws SQLException {return null;}
		public void rollback(Savepoint savepoint) throws SQLException {}
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {}
		public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {return null;}
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {return null;}
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {return null;}
		public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {return null;}
		public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {return null;}
		public Clob createClob() throws SQLException {return null;}
		public Blob createBlob() throws SQLException {return null;}
		public NClob createNClob() throws SQLException {return null;}
		public SQLXML createSQLXML() throws SQLException {return null;}
		public void setClientInfo(String name, String value) throws SQLClientInfoException {}
		public void setClientInfo(Properties properties) throws SQLClientInfoException {}
		public String getClientInfo(String name) throws SQLException {return null;}
		public Properties getClientInfo() throws SQLException {return null;}
		public Array createArrayOf(String typeName, Object[] elements) throws SQLException {return null;}
		public Struct createStruct(String typeName, Object[] attributes) throws SQLException {return null;}
		public void setSchema(String schema) throws SQLException {}
		public String getSchema() throws SQLException {return null;}
		public void abort(Executor executor) throws SQLException {}
		public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {}
		public int getNetworkTimeout() throws SQLException {return 0;}
	}
	
	private static class _PreparedStatement implements PreparedStatement
	{
		private NoSQLHandle connection = null;
		private oracle.nosql.driver.ops.PreparedStatement prepared = null;
		private _ResultSet result = null;
		
		public _PreparedStatement(oracle.nosql.driver.ops.PreparedStatement prepared, NoSQLHandle connection) throws SQLException
		{
			try
			{
				this.connection = connection;
				this.prepared = prepared;
			}
			catch(NoSQLException e) { throw new SQLException(e); }
		}
		
		public void close() throws SQLException
		{
			connection = null;
			prepared.clearVariables();
			if( result != null ) { result.close(); result = null; }
		}
		
		public ResultSet getResultSet() throws SQLException
		{
			return result;
		}

		public int getUpdateCount() throws SQLException
		{
			// we are read only so return 0
			return 0;
		}

		public ResultSet getGeneratedKeys() throws SQLException
		{
			// we are read only so return null
			return null;
		}

		public void setObject(int parameterIndex, Object x) throws SQLException
		{
			FieldValue v = null;
			if( x instanceof Boolean ) v = BooleanValue.getInstance((Boolean)x);
			else if( x instanceof Double ) v = new DoubleValue((Double)x);
			else if( x instanceof Integer ) v = new IntegerValue((Integer)x);
			else if( x instanceof Long ) v = new LongValue((Long)x);
			else if( x instanceof String ) v = new StringValue((String)x);
			else if( x instanceof BigDecimal ) v = new NumberValue((BigDecimal)x);
			else if( x instanceof Timestamp ) v = new TimestampValue((Timestamp)x);
			else if( x instanceof byte[] ) v = new BinaryValue((byte[])x);
			else if( x == null ) v = JsonNullValue.getInstance();
			else v = new StringValue(x.toString());
			
			prepared.setVariable("$param"+parameterIndex, v);
		}

		public boolean execute() throws SQLException
		{
			try
			{
				QueryRequest request = new QueryRequest().setPreparedStatement(prepared);
				byte[] continuationKey = null;
				QueryResult result = null;
				List<MapValue> values = new ArrayList<MapValue>();
				do
				{
					result = connection.query(request);
					result.getResults().forEach(item -> { values.add(item); });
		            continuationKey = result.getContinuationKey();
		        } while (continuationKey != null);
				
				this.result = new _ResultSet(values);
				
				return true;
			}
			catch(NoSQLException e) { throw new SQLException(e); }
		}
		
		public ResultSet executeQuery(String sql) throws SQLException {return null;}
		public int executeUpdate(String sql) throws SQLException {return 0;}
		public boolean execute(String sql) throws SQLException {return false;}
		public int getMaxFieldSize() throws SQLException {return 0;}
		public void setMaxFieldSize(int max) throws SQLException {}
		public int getMaxRows() throws SQLException {return 0;}
		public void setMaxRows(int max) throws SQLException {}
		public void setEscapeProcessing(boolean enable) throws SQLException {}
		public int getQueryTimeout() throws SQLException {return 0;}
		public void setQueryTimeout(int seconds) throws SQLException {}
		public void cancel() throws SQLException {}
		public SQLWarning getWarnings() throws SQLException {return null;}
		public void clearWarnings() throws SQLException {}
		public void setCursorName(String name) throws SQLException {}
		public boolean getMoreResults() throws SQLException {return false;}
		public void setFetchDirection(int direction) throws SQLException {}
		public int getFetchDirection() throws SQLException {return 0;}
		public void setFetchSize(int rows) throws SQLException {}
		public int getFetchSize() throws SQLException {return 0;}
		public int getResultSetConcurrency() throws SQLException {return 0;}
		public int getResultSetType() throws SQLException {return 0;}
		public void addBatch(String sql) throws SQLException {}
		public void clearBatch() throws SQLException {}
		public int[] executeBatch() throws SQLException {return null;}
		public Connection getConnection() throws SQLException {return null;}
		public boolean getMoreResults(int current) throws SQLException {return false;}
		public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {return 0;}
		public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {return 0;}
		public int executeUpdate(String sql, String[] columnNames) throws SQLException {return 0;}
		public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {return false;}
		public boolean execute(String sql, int[] columnIndexes) throws SQLException {return false;}
		public boolean execute(String sql, String[] columnNames) throws SQLException {return false;}
		public int getResultSetHoldability() throws SQLException {return 0;}
		public boolean isClosed() throws SQLException {return false;}
		public void setPoolable(boolean poolable) throws SQLException {}
		public boolean isPoolable() throws SQLException {return false;}
		public void closeOnCompletion() throws SQLException {}
		public boolean isCloseOnCompletion() throws SQLException {return false;}
		public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
		public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}
		public ResultSet executeQuery() throws SQLException {return null;}
		public int executeUpdate() throws SQLException {return 0;}
		public void setNull(int parameterIndex, int sqlType) throws SQLException {}
		public void setBoolean(int parameterIndex, boolean x) throws SQLException {}
		public void setByte(int parameterIndex, byte x) throws SQLException {}
		public void setShort(int parameterIndex, short x) throws SQLException {}
		public void setInt(int parameterIndex, int x) throws SQLException {}
		public void setLong(int parameterIndex, long x) throws SQLException {}
		public void setFloat(int parameterIndex, float x) throws SQLException {}
		public void setDouble(int parameterIndex, double x) throws SQLException {}
		public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {}
		public void setString(int parameterIndex, String x) throws SQLException {}
		public void setBytes(int parameterIndex, byte[] x) throws SQLException {}
		public void setDate(int parameterIndex, Date x) throws SQLException {}
		public void setTime(int parameterIndex, Time x) throws SQLException {}
		public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {}
		public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {}
		public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {}
		public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {}
		public void clearParameters() throws SQLException {}
		public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {}
		public void addBatch() throws SQLException {}
		public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {}
		public void setRef(int parameterIndex, Ref x) throws SQLException {}
		public void setBlob(int parameterIndex, Blob x) throws SQLException {}
		public void setClob(int parameterIndex, Clob x) throws SQLException {}
		public void setArray(int parameterIndex, Array x) throws SQLException {}
		public ResultSetMetaData getMetaData() throws SQLException {return null;}
		public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {}
		public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {}
		public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {}
		public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {}
		public void setURL(int parameterIndex, URL x) throws SQLException {}
		public ParameterMetaData getParameterMetaData() throws SQLException {return null;}
		public void setRowId(int parameterIndex, RowId x) throws SQLException {}
		public void setNString(int parameterIndex, String value) throws SQLException {}
		public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {}
		public void setNClob(int parameterIndex, NClob value) throws SQLException {}
		public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {}
		public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {}
		public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {}
		public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {}
		public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {}
		public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {}
		public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {}
		public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {}
		public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {}
		public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {}
		public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {}
		public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {}
		public void setClob(int parameterIndex, Reader reader) throws SQLException {}
		public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {}
		public void setNClob(int parameterIndex, Reader reader) throws SQLException {}
	}

	private static class _ResultSet implements ResultSet
	{
		private List<MapValue> values = null;
		private int i = -1;
		private List<Map.Entry<String, FieldValue>> current = null;
		
		public _ResultSet(List<MapValue> values)
		{
			this.values = values;
		}
		
		public void close() throws SQLException 
		{
			values.clear(); values = null;
			current.clear(); current = null;
		}
		
		public boolean next() throws SQLException
		{
			i++;
			if( i > values.size() )
			{
				current = null;
				return false;
			}
			
			current = new ArrayList<>(values.get(i).entrySet());
			return true;
		}
		
		public Object getObject(int columnIndex) throws SQLException
		{
			if( current == null || i < 1 || i > current.size() ) throw new SQLException();
			return toAny(current.get(columnIndex-1).getValue());
		}
		
		public ResultSetMetaData getMetaData() throws SQLException
		{
			if( current == null ) throw new SQLException();
			return new _ResultSetMetaData(current);
		}
		
		private Any toAny(FieldValue value)
		{
			if( value instanceof BooleanValue ) return Any.wrap(((BooleanValue)value).getValue());
			if( value instanceof DoubleValue ) return Any.wrap(((DoubleValue)value).getValue());
			if( value instanceof IntegerValue ) return Any.wrap(((IntegerValue)value).getValue());
			if( value instanceof LongValue ) return Any.wrap(((LongValue)value).getValue());
			if( value instanceof StringValue ) return Any.wrap(((StringValue)value).getValue());
			if( value instanceof NumberValue ) return Any.wrap(((NumberValue)value).getValue());
			if( value instanceof TimestampValue ) return Any.wrap(((TimestampValue)value).getLong());
			if( value instanceof BinaryValue ) return Any.wrap(((BinaryValue)value).getValue());
			if( value instanceof ArrayValue )
			{
				Any array = Any.emptyList();
				for( FieldValue v : ((ArrayValue)value) )
					array.add(toAny(v));
				return array;
			}
			if( value instanceof MapValue )
			{
				Any map = Any.emptyMap();
				for( Map.Entry<String, FieldValue> e : ((MapValue)value).entrySet() )
					map.put(e.getKey(), toAny(e.getValue()));
				return map;
			}
			
			// JsonNullValue
			return Any.empty();
		}
		
		public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
		public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}
		public boolean wasNull() throws SQLException {return false;}
		public String getString(int columnIndex) throws SQLException {return null;}
		public boolean getBoolean(int columnIndex) throws SQLException {return false;}
		public byte getByte(int columnIndex) throws SQLException {return 0;}
		public short getShort(int columnIndex) throws SQLException {return 0;}
		public int getInt(int columnIndex) throws SQLException {return 0;}
		public long getLong(int columnIndex) throws SQLException {return 0;}
		public float getFloat(int columnIndex) throws SQLException {return 0;}
		public double getDouble(int columnIndex) throws SQLException {return 0;}
		public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {return null;}
		public byte[] getBytes(int columnIndex) throws SQLException {return null;}
		public Date getDate(int columnIndex) throws SQLException {return null;}
		public Time getTime(int columnIndex) throws SQLException {return null;}
		public Timestamp getTimestamp(int columnIndex) throws SQLException {return null;}
		public InputStream getAsciiStream(int columnIndex) throws SQLException {return null;}
		public InputStream getUnicodeStream(int columnIndex) throws SQLException {return null;}
		public InputStream getBinaryStream(int columnIndex) throws SQLException {return null;}
		public String getString(String columnLabel) throws SQLException {return null;}
		public boolean getBoolean(String columnLabel) throws SQLException {return false;}
		public byte getByte(String columnLabel) throws SQLException {return 0;}
		public short getShort(String columnLabel) throws SQLException {return 0;}
		public int getInt(String columnLabel) throws SQLException {return 0;}
		public long getLong(String columnLabel) throws SQLException {return 0;}
		public float getFloat(String columnLabel) throws SQLException {return 0;}
		public double getDouble(String columnLabel) throws SQLException {return 0;}
		public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {return null;}
		public byte[] getBytes(String columnLabel) throws SQLException {return null;}
		public Date getDate(String columnLabel) throws SQLException {return null;}
		public Time getTime(String columnLabel) throws SQLException {return null;}
		public Timestamp getTimestamp(String columnLabel) throws SQLException {return null;}
		public InputStream getAsciiStream(String columnLabel) throws SQLException {return null;}
		public InputStream getUnicodeStream(String columnLabel) throws SQLException {return null;}
		public InputStream getBinaryStream(String columnLabel) throws SQLException {return null;}
		public SQLWarning getWarnings() throws SQLException {return null;}
		public void clearWarnings() throws SQLException {}
		public String getCursorName() throws SQLException {return null;}
		public Object getObject(String columnLabel) throws SQLException {return null;}
		public int findColumn(String columnLabel) throws SQLException {return 0;}
		public Reader getCharacterStream(int columnIndex) throws SQLException {return null;}
		public Reader getCharacterStream(String columnLabel) throws SQLException {return null;}
		public BigDecimal getBigDecimal(int columnIndex) throws SQLException {return null;}
		public BigDecimal getBigDecimal(String columnLabel) throws SQLException {return null;}
		public boolean isBeforeFirst() throws SQLException {return false;}
		public boolean isAfterLast() throws SQLException {return false;}
		public boolean isFirst() throws SQLException {return false;}
		public boolean isLast() throws SQLException {return false;}
		public void beforeFirst() throws SQLException {}
		public void afterLast() throws SQLException {}
		public boolean first() throws SQLException {return false;}
		public boolean last() throws SQLException {return false;}
		public int getRow() throws SQLException {return 0;}
		public boolean absolute(int row) throws SQLException {return false;}
		public boolean relative(int rows) throws SQLException {return false;}
		public boolean previous() throws SQLException {return false;}
		public void setFetchDirection(int direction) throws SQLException {}
		public int getFetchDirection() throws SQLException {return 0;}
		public void setFetchSize(int rows) throws SQLException {}
		public int getFetchSize() throws SQLException {return 0;}
		public int getType() throws SQLException {return 0;}
		public int getConcurrency() throws SQLException {return 0;}
		public boolean rowUpdated() throws SQLException {return false;}
		public boolean rowInserted() throws SQLException {return false;}
		public boolean rowDeleted() throws SQLException {return false;}
		public void updateNull(int columnIndex) throws SQLException {}
		public void updateBoolean(int columnIndex, boolean x) throws SQLException {}
		public void updateByte(int columnIndex, byte x) throws SQLException {}
		public void updateShort(int columnIndex, short x) throws SQLException {}
		public void updateInt(int columnIndex, int x) throws SQLException {}
		public void updateLong(int columnIndex, long x) throws SQLException {}
		public void updateFloat(int columnIndex, float x) throws SQLException {}
		public void updateDouble(int columnIndex, double x) throws SQLException {}
		public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {}
		public void updateString(int columnIndex, String x) throws SQLException {}
		public void updateBytes(int columnIndex, byte[] x) throws SQLException {}
		public void updateDate(int columnIndex, Date x) throws SQLException {}
		public void updateTime(int columnIndex, Time x) throws SQLException {}
		public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {}
		public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {}
		public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {}
		public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {}
		public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {}
		public void updateObject(int columnIndex, Object x) throws SQLException {}
		public void updateNull(String columnLabel) throws SQLException {}
		public void updateBoolean(String columnLabel, boolean x) throws SQLException {}
		public void updateByte(String columnLabel, byte x) throws SQLException {}
		public void updateShort(String columnLabel, short x) throws SQLException {}
		public void updateInt(String columnLabel, int x) throws SQLException {}
		public void updateLong(String columnLabel, long x) throws SQLException {}
		public void updateFloat(String columnLabel, float x) throws SQLException {}
		public void updateDouble(String columnLabel, double x) throws SQLException {}
		public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {}
		public void updateString(String columnLabel, String x) throws SQLException {}
		public void updateBytes(String columnLabel, byte[] x) throws SQLException {}
		public void updateDate(String columnLabel, Date x) throws SQLException {}
		public void updateTime(String columnLabel, Time x) throws SQLException {}
		public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {}
		public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {}
		public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {}
		public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {}
		public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {}
		public void updateObject(String columnLabel, Object x) throws SQLException {}
		public void insertRow() throws SQLException {}
		public void updateRow() throws SQLException {}
		public void deleteRow() throws SQLException {}
		public void refreshRow() throws SQLException {}
		public void cancelRowUpdates() throws SQLException {}
		public void moveToInsertRow() throws SQLException {}
		public void moveToCurrentRow() throws SQLException {}
		public Statement getStatement() throws SQLException {return null;}
		public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {return null;}
		public Ref getRef(int columnIndex) throws SQLException {return null;}
		public Blob getBlob(int columnIndex) throws SQLException {return null;}
		public Clob getClob(int columnIndex) throws SQLException {return null;}
		public Array getArray(int columnIndex) throws SQLException {return null;}
		public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {return null;}
		public Ref getRef(String columnLabel) throws SQLException {return null;}
		public Blob getBlob(String columnLabel) throws SQLException {return null;}
		public Clob getClob(String columnLabel) throws SQLException {return null;}
		public Array getArray(String columnLabel) throws SQLException {return null;}
		public Date getDate(int columnIndex, Calendar cal) throws SQLException {return null;}
		public Date getDate(String columnLabel, Calendar cal) throws SQLException {return null;}
		public Time getTime(int columnIndex, Calendar cal) throws SQLException {return null;}
		public Time getTime(String columnLabel, Calendar cal) throws SQLException {return null;}
		public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {return null;}
		public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {return null;}
		public URL getURL(int columnIndex) throws SQLException {return null;}
		public URL getURL(String columnLabel) throws SQLException {return null;}
		public void updateRef(int columnIndex, Ref x) throws SQLException {}
		public void updateRef(String columnLabel, Ref x) throws SQLException {}
		public void updateBlob(int columnIndex, Blob x) throws SQLException {}
		public void updateBlob(String columnLabel, Blob x) throws SQLException {}
		public void updateClob(int columnIndex, Clob x) throws SQLException {}
		public void updateClob(String columnLabel, Clob x) throws SQLException {}
		public void updateArray(int columnIndex, Array x) throws SQLException {}
		public void updateArray(String columnLabel, Array x) throws SQLException {}
		public RowId getRowId(int columnIndex) throws SQLException {return null;}
		public RowId getRowId(String columnLabel) throws SQLException {return null;}
		public void updateRowId(int columnIndex, RowId x) throws SQLException {}
		public void updateRowId(String columnLabel, RowId x) throws SQLException {}
		public int getHoldability() throws SQLException {return 0;}
		public boolean isClosed() throws SQLException {return false;}
		public void updateNString(int columnIndex, String nString) throws SQLException {}
		public void updateNString(String columnLabel, String nString) throws SQLException {}
		public void updateNClob(int columnIndex, NClob nClob) throws SQLException {}
		public void updateNClob(String columnLabel, NClob nClob) throws SQLException {}
		public NClob getNClob(int columnIndex) throws SQLException {return null;}
		public NClob getNClob(String columnLabel) throws SQLException {return null;}
		public SQLXML getSQLXML(int columnIndex) throws SQLException {return null;}
		public SQLXML getSQLXML(String columnLabel) throws SQLException {return null;}
		public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {}
		public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {}
		public String getNString(int columnIndex) throws SQLException {return null;}
		public String getNString(String columnLabel) throws SQLException {return null;}
		public Reader getNCharacterStream(int columnIndex) throws SQLException {return null;}
		public Reader getNCharacterStream(String columnLabel) throws SQLException {return null;}
		public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {}
		public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {}
		public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {}
		public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {}
		public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {}
		public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {}
		public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {}
		public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {}
		public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {}
		public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {}
		public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {}
		public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {}
		public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {}
		public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {}
		public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {}
		public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {}
		public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {}
		public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {}
		public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {}
		public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {}
		public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {}
		public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {}
		public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {}
		public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {}
		public void updateClob(int columnIndex, Reader reader) throws SQLException {}
		public void updateClob(String columnLabel, Reader reader) throws SQLException {}
		public void updateNClob(int columnIndex, Reader reader) throws SQLException {}
		public void updateNClob(String columnLabel, Reader reader) throws SQLException {}
		public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {return null;}
		public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {return null;}
	}
	
	private static class _ResultSetMetaData implements ResultSetMetaData
	{
		List<Map.Entry<String, FieldValue>> current = null;
		public _ResultSetMetaData(List<Map.Entry<String, FieldValue>> current)
		{
			this.current = current;
		}
		
		public int getColumnCount() throws SQLException
		{
			return current.size();
		}
		
		public String getColumnLabel(int column) throws SQLException
		{
			if( column < 1 || column > current.size() ) throw new SQLException(new IndexOutOfBoundsException());
			return current.get(column-1).getKey();
		}
		
		public <T> T unwrap(Class<T> iface) throws SQLException {return null;}
		public boolean isWrapperFor(Class<?> iface) throws SQLException {return false;}
		public boolean isAutoIncrement(int column) throws SQLException {return false;}
		public boolean isCaseSensitive(int column) throws SQLException {return false;}
		public boolean isSearchable(int column) throws SQLException {return false;}
		public boolean isCurrency(int column) throws SQLException {return false;}
		public int isNullable(int column) throws SQLException {return 0;}
		public boolean isSigned(int column) throws SQLException {return false;}
		public int getColumnDisplaySize(int column) throws SQLException {return 0;}
		public String getColumnName(int column) throws SQLException {return null;}
		public String getSchemaName(int column) throws SQLException {return null;}
		public int getPrecision(int column) throws SQLException {return 0;}
		public int getScale(int column) throws SQLException {return 0;}
		public String getTableName(int column) throws SQLException {return null;}
		public String getCatalogName(int column) throws SQLException {return null;}
		public int getColumnType(int column) throws SQLException {return 0;}
		public String getColumnTypeName(int column) throws SQLException {return null;}
		public boolean isReadOnly(int column) throws SQLException {return false;}
		public boolean isWritable(int column) throws SQLException {return false;}
		public boolean isDefinitelyWritable(int column) throws SQLException {return false;}
		public String getColumnClassName(int column) throws SQLException {return null;}
	}
}
