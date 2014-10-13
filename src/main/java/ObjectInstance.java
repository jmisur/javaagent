class ObjectInstance {
    private int systemId;
    private String className;
    private Object value;

    ObjectInstance(Object value) {
        systemId = System.identityHashCode(value);
        className = value.getClass().getName();
        this.value = value;
    }
}
