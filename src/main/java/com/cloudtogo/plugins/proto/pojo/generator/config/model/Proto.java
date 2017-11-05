package com.cloudtogo.plugins.proto.pojo.generator.config.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;

/**
 * Created by cheney on 2017/10/26.
 */
@XStreamAlias("proto")
public class Proto {

    private String file;

    @XStreamAlias("pojo-pkg")
    private String pojo;

    @XStreamAlias("builder-pkg")
    private String builder;

    private Protoc protoc;

    private List<SpecialField> fields;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPojo() {
        return pojo;
    }

    public void setPojo(String pojo) {
        this.pojo = pojo;
    }

    public String getBuilder() {
        return builder;
    }

    public void setBuilder(String builder) {
        this.builder = builder;
    }

    public Protoc getProtoc() {
        return protoc;
    }

    public void setProtoc(Protoc protoc) {
        this.protoc = protoc;
    }

    public List<SpecialField> getFields() {
        return fields;
    }

    public void setFields(List<SpecialField> fields) {
        this.fields = fields;
    }
}
