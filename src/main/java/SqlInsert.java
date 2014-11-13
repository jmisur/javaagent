public class SqlInsert extends SqlOperation {

    private Object[] generatedKeys;

    public SqlInsert(String sqlId, String sql, Integer updated, Object[] generatedKeys) {
        super(sqlId, sql);
        setAffectedRows(updated);
        this.generatedKeys = generatedKeys;
    }

    @Override
    protected String getOpType() {
        return "insert";
    }
}
