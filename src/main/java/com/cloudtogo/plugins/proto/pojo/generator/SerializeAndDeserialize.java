package com.cloudtogo.plugins.proto.pojo.generator;

import java.io.Serializable;

/**
 * Created by cheney on 2017/10/24.
 */
public class SerializeAndDeserialize implements Serializable {

    private String message;

    private String field;

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
