package org.lorislab.p6.engine.domain.models;

import org.lorislab.p6.bpmn2.model.FlowElement;
import org.lorislab.p6.engine.domain.store.model.Stream;
import org.lorislab.p6.engine.domain.store.model.event.TokenEvent;

import io.vertx.mutiny.sqlclient.SqlClient;

public class TokenEventContext<T extends FlowElement<?>> extends EventContext<TokenEvent> {

    private final ProcessDefinitionData processDefinitionData;

    private final T element;

    public static <E extends FlowElement<?>> TokenEventContext<E> of(EventContext<TokenEvent> ctx, ProcessDefinitionData pd,
            E element) {
        return new TokenEventContext<>(ctx.getStream(), ctx.getClient(), ctx.getEvent(), pd, element);
    }

    public TokenEventContext(Stream stream, SqlClient client, TokenEvent event, ProcessDefinitionData pd, T element) {
        super(stream, client, event);
        this.processDefinitionData = pd;
        this.element = element;
    }

    public ProcessDefinitionData getProcessDefinitionData() {
        return processDefinitionData;
    }

    public T getElement() {
        return element;
    }
}
