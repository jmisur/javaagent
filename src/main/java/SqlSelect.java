public class SqlSelect extends SqlOperation {

    private String[] columns;

    public SqlSelect(String sqlId, String sql, String[] columns) {
        super(sqlId, sql);
        this.columns = columns;
    }

    @Override
    protected String getOpType() {
        return "select";
    }
}
