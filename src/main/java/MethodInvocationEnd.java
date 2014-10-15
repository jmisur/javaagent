public class MethodInvocationEnd extends MethodInvocation {
    private Object result;

    @Override
    protected String getPhase() {
        return "after";
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
