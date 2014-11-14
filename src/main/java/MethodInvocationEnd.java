import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class MethodInvocationEnd extends MethodInvocation {
    @JsonInclude(NON_NULL)
    private Object result;

    @JsonInclude(NON_NULL)
    private Object exception;

    @Override
    protected String getPhase() {
        return "after";
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setException(Object exception) {
        this.exception = exception;
    }
}
