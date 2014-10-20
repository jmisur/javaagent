import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

class Parameter {
    @JsonIgnore
    private String name;
    @JsonUnwrapped
    private Object value;

    Parameter(String name, Object value) {
        assert name != null;
        assert value != null;

        this.name = name;
        this.value = value;
    }

    Object getValue() {
        return value;
    }

    String getName() {
        return name;
    }
}
