package com.cloudtogo.plugins.proto.pojo.generator;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * Created by cheney on 2017/10/6.
 */
public class ProtoModule implements Serializable {

    private File proto;

    private String pkg;

    List<SerializeAndDeserialize> fields;

    public File getProto() {
        return proto;
    }

    public void setProto(File proto) {
        this.proto = proto;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public List<SerializeAndDeserialize> getFields() {
        return fields;
    }

    public void setFields(List<SerializeAndDeserialize> fields) {
        this.fields = fields;
    }
}
