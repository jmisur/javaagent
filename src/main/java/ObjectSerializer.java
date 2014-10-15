import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;

class ObjectSerializer extends BeanSerializer {

    ObjectSerializer(BeanSerializerBase source) {
        super(source);
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeNumberField("systemId", System.identityHashCode(bean));
        jgen.writeStringField("className", bean.getClass().getCanonicalName());
        super.serializeFields(bean, jgen, provider);
    }
}