class TransactionBoundary {
    private String correlationId = CorrelationIdHolder.get();
    private String type = "transactionBoundary";
    private int propagation;
    private long millis = System.currentTimeMillis();
    private String threadName;
    private String joinpointIdentification;
    private boolean isNew;

    void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setJoinpointIdentification(String joinpointIdentification) {
        this.joinpointIdentification = joinpointIdentification;
    }

    public void setPropagation(int propagation) {
        this.propagation = propagation;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}
