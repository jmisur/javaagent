import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class SimpleMain {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Writer writer;
    private static ObjectWriter objectWriter;
    private static boolean written = false;
    private static boolean paused;

    public static void premain(String agentArguments, Instrumentation instrumentation) throws IOException, UnmodifiableClassException {
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

//        System.out.println("loaded:");
//        for (Class clazz : instrumentation.getAllLoadedClasses()) {
//            System.out.println(clazz);
//        }
//        System.out.println("initiated:");
//        for (Class clazz : instrumentation.getInitiatedClasses(SimpleMain.class.getClassLoader())) {
//            System.out.println(clazz);
//        }

        instrumentation.addTransformer(new SimpleTransformer());
    }

    public static void before(String methodName, String[] paramNames, Object[] paramValues) {
        MethodInvocationStart mi = new MethodInvocationStart();
        mi.setParams(paramNames, paramValues);
        mi.setSignature(methodName);

        log(mi);
    }

    public static void after(String methodName, String[] paramNames, Object[] paramValues, Object result) {
        MethodInvocationEnd mi = new MethodInvocationEnd();
        mi.setParams(paramNames, paramValues);
        mi.setSignature(methodName);
        mi.setResult(result);

        log(mi);
    }

    static void log(Object o) {
        if (writer == null || paused) return;

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

        paused = false;
        writer.append("[");
    }

    static void start(Writer writer) throws IOException {
        SimpleMain.writer = writer;
        start();
    }

    static void start(PrintStream stream) throws IOException {
        start(new PrintWriter(stream));
    }

    static void stop() throws IOException {
        if (writer == null) return;

        writer.append("]");
        writer.close();
        writer = null;
        written = false;
        paused = false;
    }

    public static void newTx(String joinpointIdentification, boolean isNew, int propagation) {
        TransactionBoundary tx = new TransactionBoundary();
        tx.setJoinpointIdentification(joinpointIdentification);
        tx.setNew(isNew);
        tx.setPropagation(propagation);
        log(tx);
    }

    static void pause() {
        paused = true;
    }

    static void resume() {
        paused = false;
    }
}