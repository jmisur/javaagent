public class CorrelationIdHolder {
    private static final ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    public static void remove() {
        threadLocal.remove();
    }

    public static void set(String correlationId) {
        threadLocal.set(correlationId);
    }

    public static String get() {
        String value = threadLocal.get();
        if (value == null) {
            value = Id.random();
            set(value);
        }
        return value;
    }
}
