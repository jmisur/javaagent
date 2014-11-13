import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StatementWrapper implements Statement {
    private final Statement target;
    private String currentSqlId;

    protected StatementWrapper(Statement target) {
        this.target = target;
    }

    public static StatementWrapper wrap(Statement statement) {
        return new StatementWrapper(statement);
    }

    protected Statement target() {
        return target;
    }

    private String newId() {
        return Id.random();
    }

    private int log(String sql, int updated) throws SQLException {
        currentSqlId = newId();
        logInternal(currentSqlId, sql, Integer.valueOf(updated));
        return updated;
    }

    private ResultSet log(String sql, ResultSet rs) throws SQLException {
        currentSqlId = newId();
        logInternal(currentSqlId, sql, null);
        return ResultSetWrapper.wrap(currentSqlId, rs);
    }

    private boolean log(String sql, boolean result) throws SQLException {
        currentSqlId = newId(); // remember for result set

        if (result) {
            logInternal(currentSqlId, sql, null);
        } else {
            logInternal(currentSqlId, sql, Integer.valueOf(target.getUpdateCount()));
        }

        return result;
    }

    private void logInternal(String sqlId, String sql, Integer updated) throws SQLException {
        if (sql.startsWith("update")) // TODO ignore case, maybe regexp
            SimpleMain.log(new SqlUpdate(sqlId, sql, updated));
        else if (sql.startsWith("insert"))
            SimpleMain.log(new SqlInsert(sqlId, sql, updated, keys()));
        else if (sql.startsWith("select"))
            SimpleMain.log(new SqlSelect(sqlId, sql, columns()));
        else if (sql.startsWith("delete"))
            SimpleMain.log(new SqlDelete(sqlId, sql, updated));
    }

    private String[] columns() throws SQLException {
        ResultSetMetaData metaData = target.getResultSet().getMetaData();
        int count = metaData.getColumnCount();

        String[] names = new String[count];
        for (int i = 0; i < count; i++)
            names[i] = metaData.getColumnName(i + 1).toLowerCase();

        return names;
    }

    private int[] log(int[] updated) throws SQLException {
//        if (sql.startsWith("update"))
//            SimpleMain.log(new SqlUpdate(sql, updated));
//        else if (sql.startsWith("insert"))
//            SimpleMain.log(new SqlInsert(sql, updated, keys()));
        return updated;
    }

    private Object[] keys() throws SQLException {
        List<Object> keys = new ArrayList<>();
        ResultSet rs = target().getGeneratedKeys();
        while (rs != null && rs.next()) {
            keys.add(rs.getObject(1));
        }
        return keys.toArray(new Object[keys.size()]);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        return log(sql, target.executeQuery(sql));
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        return log(sql, target.executeUpdate(sql));
    }

    @Override
    public void close() throws SQLException {
        target.close();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        return target.getMaxFieldSize();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        target.setMaxFieldSize(max);
    }

    @Override
    public int getMaxRows() throws SQLException {
        return target.getMaxRows();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        target.setMaxRows(max);
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        target.setEscapeProcessing(enable);
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        return target.getQueryTimeout();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        target.setQueryTimeout(seconds);
    }

    @Override
    public void cancel() throws SQLException {
        target.cancel();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return target.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        target.clearWarnings();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        target.setCursorName(name);
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        return log(sql, target.execute(sql));
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return ResultSetWrapper.wrap(currentSqlId, target.getResultSet());
    }

    @Override
    public int getUpdateCount() throws SQLException {
        return target.getUpdateCount();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        return target.getMoreResults();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        target.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return target.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        target.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return target.getFetchSize();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        return target.getResultSetConcurrency();
    }

    @Override
    public int getResultSetType() throws SQLException {
        return target.getResultSetType();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        target.addBatch(sql);
    }

    @Override
    public void clearBatch() throws SQLException {
        target.clearBatch();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        return log(target.executeBatch());
    }

    @Override
    public Connection getConnection() throws SQLException {
        return target.getConnection();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        return target.getMoreResults(current);
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        return target.getGeneratedKeys();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return log(sql, target.executeUpdate(sql, autoGeneratedKeys));
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return log(sql, target.executeUpdate(sql, columnIndexes));
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return log(sql, target.executeUpdate(sql, columnNames));
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return log(sql, target.execute(sql, autoGeneratedKeys));
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return log(sql, target.execute(sql, columnIndexes));
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return log(sql, target.execute(sql, columnNames));
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        return target.getResultSetHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return target.isClosed();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        target.setPoolable(poolable);
    }

    @Override
    public boolean isPoolable() throws SQLException {
        return target.isPoolable();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        target.closeOnCompletion();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return target.isCloseOnCompletion();
    }

    @Override
    public long getLargeUpdateCount() throws SQLException {
        return target.getLargeUpdateCount();
    }

    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        target.setLargeMaxRows(max);
    }

    @Override
    public long getLargeMaxRows() throws SQLException {
        return target.getLargeMaxRows();
    }

    @Override
    public long[] executeLargeBatch() throws SQLException {
        return target.executeLargeBatch();
    }

    @Override
    public long executeLargeUpdate(String sql) throws SQLException {
        return target.executeLargeUpdate(sql);
    }

    @Override
    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return target.executeLargeUpdate(sql, autoGeneratedKeys);
    }

    @Override
    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return target.executeLargeUpdate(sql, columnIndexes);
    }

    @Override
    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return target.executeLargeUpdate(sql, columnNames);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return target.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return target.isWrapperFor(iface);
    }
}
