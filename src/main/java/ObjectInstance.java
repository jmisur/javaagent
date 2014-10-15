class ObjectInstance {
    private int systemId;
    private String className;
    private Object value;

    ObjectInstance(Object object) {
        assert object != null;

        systemId = System.identityHashCode(object);
        className = object.getClass().getCanonicalName();
        this.value = object;
    }
}
