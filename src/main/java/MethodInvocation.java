import java.util.ArrayList;
import java.util.List;

abstract class MethodInvocation {
    private String correlationId = CorrelationIdHolder.get();
    private String type = "methodInvocation";
    private String phase = getPhase();
    private long millis = System.currentTimeMillis();
    private String threadName;
    private String signature;
    private List<Parameter> params;

    protected abstract String getPhase();

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
