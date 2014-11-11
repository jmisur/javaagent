import com.fasterxml.jackson.annotation.JsonInclude;

public abstract class SqlOperation extends RecordObject {
    private String opType = getOpType();
    private String query;
    private Object[] params = new Object[0];
    private int affectedRows = 0;

    @Override
    @JsonInclude
    protected String getType() {
        return "sqlOperation";
    }

    protected abstract String getOpType();

    public void setQuery(String query) {
        this.query = query;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public void setAffectedRows(int affectedRows) {
        this.affectedRows = affectedRows;
    }
}