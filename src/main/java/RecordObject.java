public abstract class RecordObject {
    private String correlationId = CorrelationIdHolder.get();
    private String type = getType();
    private long millis = System.currentTimeMillis();
    private String threadName = Thread.currentThread().getName();

    protected abstract String getType();

}
