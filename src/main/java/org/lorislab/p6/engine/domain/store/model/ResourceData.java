package org.lorislab.p6.engine.domain.store.model;

import org.lorislab.quarkus.data.Column;
import org.lorislab.quarkus.data.Entity;
import org.lorislab.quarkus.data.Id;
import org.lorislab.quarkus.data.Table;

@Entity
@Table(name = "RESOURCE_DATA")
public class ResourceData {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "DATA")
    private byte[] data;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
