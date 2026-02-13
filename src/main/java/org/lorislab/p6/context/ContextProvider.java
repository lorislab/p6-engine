package org.lorislab.p6.context;

import java.util.Map;

import org.eclipse.microprofile.context.spi.ThreadContextProvider;
import org.eclipse.microprofile.context.spi.ThreadContextSnapshot;

public class ContextProvider implements ThreadContextProvider {

    @Override
    public ThreadContextSnapshot currentContext(Map<String, String> props) {
        var captured = Context.get();
        return () -> {
            var current = restore(captured);
            return () -> restore(current);
        };
    }

    @Override
    public ThreadContextSnapshot clearedContext(Map<String, String> props) {
        return () -> {
            var current = restore(null);
            return () -> restore(current);
        };
    }

    @Override
    public String getThreadContextType() {
        return "p6";
    }

    private ApplicationContext restore(ApplicationContext context) {
        var currentContext = Context.get();
        Context.set(context);
        return currentContext;
    }
}
