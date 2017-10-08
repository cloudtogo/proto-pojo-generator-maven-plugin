package com.cloudtogo.plugins.proto.pojo.generator;

import java.io.File;
import java.io.Serializable;

/**
 * Created by cheney on 2017/10/6.
 */
public class ProtoModule implements Serializable {

    private File source;

    private String pkg;

    public File getSource() {
        return source;
    }

    public void setSource(File source) {
        this.source = source;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }
}
