package org.lorislab.p6.engine.domain.store;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;

import org.lorislab.quarkus.data.sql.Criteria;
import org.lorislab.quarkus.data.sql.Select;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.*;

public class AbstractRepository {

    @Inject
    Pool pool;

    public Uni<Long> selectCount(SqlClient client, Select select, Criteria criteria, String id) {
        return client.preparedQuery(select.selectCount(id).build())
                .execute(tuple(criteria.values()))
                .map(RowSet::iterator)
                .map(i -> i.hasNext() ? i.next().getLong(0) : 0);
    }

    public static Tuple tuple(Object data) {
        return Tuple.of(data);
    }

    public static Tuple tuple(List<Object> values) {
        return Tuple.tuple(values);
    }

    public static Tuple tuple(Object d1, Object d2) {
        return Tuple.of(d1, d2);
    }

    public static Tuple tuple(Object... data) {
        return Tuple.tuple(Arrays.asList(data));
    }
}
