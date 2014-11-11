public class SqlUpdate extends SqlOperation {

    public SqlUpdate(String sql, int updated) {
        setQuery(sql);
        setAffectedRows(updated);
    }

    @Override
    protected String getOpType() {
        return "update";
    }
}
