package com.cloudtogo.plugins.proto.pojo.generator.config.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Created by cheney on 2017/10/26.
 */
@XStreamAlias("field")
public class SpecialField {

    private String message;
    private String field;
    private String alias;
    private String serialize;
    private String deserialize;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getSerialize() {
        return serialize;
    }

    public void setSerialize(String serialize) {
        this.serialize = serialize;
    }

    public String getDeserialize() {
        return deserialize;
    }

    public void setDeserialize(String deserialize) {
        this.deserialize = deserialize;
    }
}
