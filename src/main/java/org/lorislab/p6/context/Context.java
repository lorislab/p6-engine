package org.lorislab.p6.context;

public class Context {

    private static final ThreadLocal<ApplicationContext> CONTEXT = new ThreadLocal<>();

    private Context() {
    }

    public static ApplicationContext get() {
        return CONTEXT.get();
    }

    public static void set(ApplicationContext ctx) {
        CONTEXT.set(ctx);
    }

    public static void close() {
        CONTEXT.remove();
    }

    public static boolean isEmpty() {
        return get() == null;
    }
}
