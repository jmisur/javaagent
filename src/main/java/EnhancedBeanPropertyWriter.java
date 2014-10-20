import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

class EnhancedBeanPropertyWriter extends BeanPropertyWriter {

    protected EnhancedBeanPropertyWriter(BeanPropertyWriter base) {
        super(base, new SerializedString("value"));
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception {
        jgen.writeStartObject();

        Object field = get(bean);
        if (field != null) {
            jgen.writeNumberField("systemId", System.identityHashCode(field));
            jgen.writeStringField("className", field.getClass().getCanonicalName());
        }
        jgen.writeStringField("name", _field.getName());

        super.serializeAsField(bean, jgen, prov);
        jgen.writeEndObject();
    }
}

