class TransactionBoundary extends RecordObject {
    private int propagation;
    private String joinpointIdentification;
    private boolean isNew;

    @Override
    protected String getType() {
        return "transactionBoundary";
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
