package com.cloudtogo.plugins.proto.pojo.generator;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by cheney on 2017/10/6.
 */
public class ProtoModule implements Serializable {

    private File proto;

    private String model;

    private String builder;

    List<SerializeAndDeserialize> fields;

    public File getProto() {
        return proto;
    }

    public void setProto(File proto) {
        this.proto = proto;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBuilder() {
        return builder;
    }

    public void setBuilder(String builder) {
        this.builder = builder;
    }

    public List<SerializeAndDeserialize> getFields() {
        return fields;
    }

    public void setFields(List<SerializeAndDeserialize> fields) {
        this.fields = fields;
    }
}
