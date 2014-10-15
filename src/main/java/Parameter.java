class Parameter {
    private String name;
    private Object value;

    Parameter(String name, Object value) {
        assert name != null;
        assert value != null;

        this.name = name;
        this.value = value;
    }
}
