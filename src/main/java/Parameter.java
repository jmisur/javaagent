class Parameter {

    private String name;
    private ObjectInstance value;

    Parameter(String name, Object value) {
        this.name = name;
        this.value = value != null ? new ObjectInstance(value) : null;
    }
}
