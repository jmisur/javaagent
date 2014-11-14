import java.util.ArrayList;
import java.util.List;

abstract class MethodInvocation extends RecordObject {
    private String phase = getPhase();
    private String signature;
    private List<Parameter> params;

    protected abstract String getPhase();

    @Override
    protected String getType() {
        return "methodInvocation";
    }

    void setParams(String[] paramNames, Object[] paramValues) {
        params = new ArrayList<>(paramValues.length);

        for (int i = 0; i < paramValues.length; i++) {
            params.add(new Parameter(paramNames[i], paramValues[i]));
        }
    }

    void setSignature(String signature) {
        this.signature = signature;
    }

}
