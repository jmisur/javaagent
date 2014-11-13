import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class SqlOperation extends RecordObject {
    private String sqlId;
    private String opType = getOpType();
    private String query;
    private Object[] params = new Object[0];
    private Integer affectedRows;

    public SqlOperation(String sqlId, String sql) {
        this.sqlId = sqlId;
        query = sql;
    }

    @Override
    @JsonInclude
    protected String getType() {
        return "sqlOperation";
    }

    protected abstract String getOpType();

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void setAffectedRows(Integer affectedRows) {
        this.affectedRows = affectedRows;
    }
}