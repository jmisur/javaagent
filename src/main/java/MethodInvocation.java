import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class MethodInvocation {
    private String type = "methodInvocation";
    private Date timestamp = new Date();
    private String threadName;
    private String signature;
    private List<Parameter> params;

    void setParams(String[] paramNames, Object[] paramValues) {
        params = new ArrayList<>(paramValues.length);

        for (int i = 0; i < paramValues.length; i++) {
            params.add(new Parameter(paramNames[i], paramValues[i]));
        }
    }

    void setSignature(String signature) {
        this.signature = signature;
    }

    void setThreadName(String threadName) {
        this.threadName = threadName;
    }
}
