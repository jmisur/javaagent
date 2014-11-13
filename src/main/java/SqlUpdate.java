public class SqlUpdate extends SqlOperation {

    public SqlUpdate(String sqlId, String sql, Integer updated) {
        super(sqlId, sql);
        setAffectedRows(updated);
    }

    @Override
    protected String getOpType() {
        return "update";
    }
}
