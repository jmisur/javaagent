public class SqlDelete extends SqlOperation {

    public SqlDelete(String sqlId, String sql, Integer updated) {
        super(sqlId, sql);
        setAffectedRows(updated);
    }

    @Override
    protected String getOpType() {
        return "delete";
    }
}
