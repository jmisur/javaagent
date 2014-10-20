import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.instrument.Instrumentation;

public class SimpleMain {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Writer writer;
    private static ObjectWriter objectWriter;
    private static boolean written = false;

    public static void premain(String agentArguments, Instrumentation instrumentation) throws IOException {
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new BeanSerializerModifier() {
            @Override
            public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
                if (serializer instanceof BeanSerializerBase)
                    return new ObjectSerializer((BeanSerializerBase) serializer);
                return serializer;
            }
        });
        mapper.registerModule(module);
        objectWriter = mapper.writer().withDefaultPrettyPrinter();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    SimpleMain.stop();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        instrumentation.addTransformer(new SimpleTransformer());
    }

    public static void before(String methodName, String[] paramNames, Object[] paramValues) {
        MethodInvocationStart mi = new MethodInvocationStart();
        mi.setParams(paramNames, paramValues);
        mi.setSignature(methodName);
        mi.setThreadName(Thread.currentThread().getName());

        log(mi);
    }

    public static void after(String methodName, String[] paramNames, Object[] paramValues, Object result) {
        MethodInvocationEnd mi = new MethodInvocationEnd();
        mi.setParams(paramNames, paramValues);
        mi.setSignature(methodName);
        mi.setThreadName(Thread.currentThread().getName());
        mi.setResult(result);

        log(mi);
    }

    private static void log(Object o) {
        if (writer == null) return;

        try {
            if (written) {
                writer.append(",");
            }
            written = true;
            objectWriter.writeValue(writer, o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void start() throws IOException {
        if (writer == null)
            writer = new FileWriter("capture.json");

        writer.append("[");
    }

    static void stop() throws IOException {
        if (writer == null) return;

        writer.append("]");
        writer.close();
        writer = null;
    }

    static void init(StringWriter writer) {
        SimpleMain.writer = writer;
    }
}