public class SqlResultSet extends RecordObject {
    private String sqlId;
    private Object[] values;

    public SqlResultSet(String sqlId, Object[] values) {
        this.sqlId = sqlId;
        this.values = values;
    }

    @Override
    protected String getType() {
        return "resultSet";
    }
}
