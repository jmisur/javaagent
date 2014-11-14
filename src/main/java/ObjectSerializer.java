import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

class ObjectSerializer extends BeanSerializer {

    private Set<String> primitives = newHashSet("java.lang.Boolean", "java.lang.Byte", "java.lang.Character",
            "java.lang.Float", "java.lang.Integer", "java.lang.Long", "java.lang.Short", "java.lang.Double",
            "java.lang.String", "java.math.BigInteger", "java.math.BigDecimal");

    private Set<String> primitiveArrays = newHashSet("java.lang.Boolean[]", "java.lang.Byte[]", "java.lang.Character[]",
            "java.lang.Float[]", "java.lang.Integer[]", "java.lang.Long[]", "java.lang.Short[]", "java.lang.Double[]",
            "java.lang.String[]", "java.math.BigInteger[]", "java.math.BigDecimal[]");

    ObjectSerializer(BeanSerializerBase source) {
        super(source);
        if (source.handledType().getPackage() == null)
            return;

        for (int i = 0; i < _props.length; i++) {
            _props[i] = new EnhancedBeanPropertyWriter(_props[i]);
        }
    }

    @Override
    protected void serializeFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
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
        return primitives.contains(bean.getClass().getCanonicalName())
                || primitiveArrays.contains(bean.getClass().getCanonicalName());
    }

    private void wrapFields(Object bean, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeArrayFieldStart("fields");
        super.serializeFields(bean, jgen, provider);
        jgen.writeEndArray();
    }

}