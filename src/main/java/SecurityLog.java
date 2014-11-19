import com.fasterxml.jackson.annotation.JsonInclude;

public class SecurityLog extends RecordObject{
    private String targetMethod;
    private String annotation;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean accessDenied;

    @Override
    protected String getType() {
        return "security";
    }

    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public void setAccessDenied(boolean accessDenied) {
        this.accessDenied = accessDenied;
    }
}
