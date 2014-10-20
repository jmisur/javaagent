import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;

class ObjectSerializer extends BeanSerializer {

    ObjectSerializer(BeanSerializerBase source) {
        super(source);
        if (source.handledType().getPackage() == null)
            return;

        for (int i = 0; i < _props.length; i++) {
            _props[i] = new EnhancedBeanPropertyWriter(_props[i]);
        }
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        if (bean instanceof Parameter) {
            jgen.writeNumberField("systemId", System.identityHashCode(((Parameter) bean).getValue()));
            jgen.writeStringField("className", ((Parameter) bean).getValue().getClass().getCanonicalName());
            jgen.writeStringField("name", ((Parameter) bean).getName());

            if (isPrimitive(((Parameter) bean).getValue())) {
                super.serializeFields(bean, jgen, provider);
            } else {
                wrapFields(bean, jgen, provider);
            }
        } else if (bean.getClass().getPackage() == null) {
            super.serializeFields(bean, jgen, provider);
        } else {
            jgen.writeNumberField("systemId", System.identityHashCode(bean));
            jgen.writeStringField("className", bean.getClass().getCanonicalName());

            if (isPrimitive(bean)) {
                super.serializeFields(bean, jgen, provider);
            } else {
                wrapFields(bean, jgen, provider);
            }
        }
    }

    private boolean isPrimitive(Object bean) {
        return bean.getClass().getCanonicalName().startsWith("java.lang");
    }

    private void wrapFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeArrayFieldStart("fields");
        super.serializeFields(bean, jgen, provider);
        jgen.writeEndArray();
    }

}