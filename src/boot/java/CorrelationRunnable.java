public class CorrelationRunnable implements Runnable {
    private final String correlationId;
    private final Runnable runnable;

    public CorrelationRunnable(String correlationId, Runnable runnable) {
        this.correlationId = correlationId;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        if (runnable == null) return;

        try {
            CorrelationIdHolder.set(correlationId);
            try {
                runnable.run();
            } finally {
                CorrelationIdHolder.remove();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
