public class SqlInsert extends SqlOperation {

    private Object[] generatedKeys;

    public SqlInsert(String sql, int updated, Object[] generatedKeys) {
        setQuery(sql);
        setAffectedRows(updated);
        this.generatedKeys = generatedKeys;
    }

    @Override
    protected String getOpType() {
        return "insert";
    }
}
