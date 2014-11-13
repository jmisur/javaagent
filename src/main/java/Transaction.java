class Transaction extends RecordObject {

    private int objectId;
    private String phase;
    private String name;

    @Override
    protected String getType() {
        return "transaction";
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public void setName(String name) {
        this.name = name;
    }
}
