import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.expression.method.ExpressionAttributeHelper;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class SimpleMain {
    private static ObjectMapper mapper = new ObjectMapper();
    private static Writer writer;
    private static ObjectWriter objectWriter;
    private static boolean written = false;
    private static boolean paused = false;
    private static boolean close = true;

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

    public static void ex(String methodName, String[] paramNames, Object[] paramValues, Object exception) {
        MethodInvocationEnd mi = new MethodInvocationEnd();
        mi.setParams(paramNames, paramValues);
        mi.setSignature(methodName);
        mi.setException(exception);

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
        close = true;
        written = false;
        writer.append("[");
    }

    static void start(Writer writer) throws IOException {
        SimpleMain.writer = writer;
        start();
    }

    static void start(PrintStream stream, boolean close) throws IOException {
        start(new PrintWriter(stream));
        SimpleMain.close = close;
    }

    static void stop() throws IOException {
        if (writer == null) return;

        writer.append("]");
        writer.flush();
        if (close) writer.close();
        writer = null;
        paused = false;
    }

    public static void newTx(Object object, TransactionDefinition definition) {
        Transaction tx = new Transaction();
        tx.setObjectId(System.identityHashCode(object));
        tx.setPhase("begin");
        tx.setName(definition.getName());
        log(tx);
    }

    public static void commitTx(DefaultTransactionStatus status) {
        Transaction tx = new Transaction();
        tx.setObjectId(System.identityHashCode(status.getTransaction()));
        tx.setPhase("commit");
        log(tx);
    }

    public static void rollbackTx(DefaultTransactionStatus status) {
        Transaction tx = new Transaction();
        tx.setObjectId(System.identityHashCode(status.getTransaction()));
        tx.setPhase("rollback");
        log(tx);
    }

    public static void logSec(Object target, Collection<ConfigAttribute> attrs) {
        SecurityLog log = new SecurityLog();
        log.setAnnotation(ExpressionAttributeHelper.getExpression(attrs.iterator().next()));
        log.setTargetMethod(getLongName((ReflectiveMethodInvocation) target));
        log(log);
    }

    public static void logSecFailed(Object target, Collection<ConfigAttribute> attrs) {
        SecurityLog log = new SecurityLog();
        log.setAnnotation(ExpressionAttributeHelper.getExpression(attrs.iterator().next()));
        log.setTargetMethod(getLongName((ReflectiveMethodInvocation) target));
        log.setAccessDenied(true);
        log(log);
    }

    private static String getLongName(ReflectiveMethodInvocation method) {
        return method.getThis().getClass().getCanonicalName() + "." + method.getMethod().getName()
                + "(" + Joiner.on(",").join(getParamTypeNames(method.getMethod().getParameterTypes())) + ")";
    }

    private static List<String> getParamTypeNames(Class<?>[] parameterTypes) {
        return FluentIterable.from(newArrayList(parameterTypes)).transform(new Function<Class<?>, String>() {
            @Override
            public String apply(Class<?> input) {
                return input.getCanonicalName();
            }
        }).toList();
    }

    static void pause() {
        paused = true;
    }

    static void resume() {
        paused = false;
    }
}