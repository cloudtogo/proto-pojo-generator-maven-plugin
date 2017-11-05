package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.jprotobuf.com.squareup.protoparser.EnumElement;

/**
 * Created by cheney on 2017/10/30.
 */
public class ProtoEnumClass {

    // proto file configed
    private String file;

    // proto file configed
    private String pkg;

    // enum name in proto file
    private String name;

    private String pojoPkg;
    private EnumElement type;

    public String getPojoClassName() {
        return this.getName();
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPojoPkg() {
        return pojoPkg;
    }

    public void setPojoPkg(String pojoPkg) {
        this.pojoPkg = pojoPkg;
    }

    public EnumElement getType() {
        return type;
    }

    public void setType(EnumElement type) {
        this.type = type;
    }
}
