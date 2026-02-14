package org.lorislab.p6.context;

public class Context {

    private static final ThreadLocal<ApplicationContext> CONTAINER = new ThreadLocal<>();

    private Context() {
    }

    public static ApplicationContext get() {
        return CONTAINER.get();
    }

    public static void set(ApplicationContext ctx) {
        CONTAINER.set(ctx);
    }

    public static void close() {
        CONTAINER.remove();
    }

    public static boolean isEmpty() {
        return get() == null;
    }
}
