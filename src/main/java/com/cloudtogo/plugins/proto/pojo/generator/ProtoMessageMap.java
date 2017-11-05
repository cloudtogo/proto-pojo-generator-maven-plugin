package com.cloudtogo.plugins.proto.pojo.generator;

/**
 * Created by cheney on 2017/10/30.
 */
public class ProtoMessageMap {

    // proto file configed
    private String file;

    // proto file configed
    private String pkg;

    // message name in proto file
    private String name;

    private String pojoClassName;
    private String pojoPkg;

    private String builderClassName;
    private String builderPkg;

    // the default
    private String protocClassName;
    private String protocClassPkg;

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

    public String getPojoClassName() {
        return pojoClassName;
    }

    public void setPojoClassName(String pojoClassName) {
        this.pojoClassName = pojoClassName;
    }

    public String getPojoPkg() {
        return pojoPkg;
    }

    public void setPojoPkg(String pojoPkg) {
        this.pojoPkg = pojoPkg;
    }

    public String getBuilderClassName() {
        return builderClassName;
    }

    public void setBuilderClassName(String builderClassName) {
        this.builderClassName = builderClassName;
    }

    public String getBuilderPkg() {
        return builderPkg;
    }

    public void setBuilderPkg(String builderPkg) {
        this.builderPkg = builderPkg;
    }

    public String getProtocClassName() {
        return protocClassName;
    }

    public void setProtocClassName(String protocClassName) {
        this.protocClassName = protocClassName;
    }

    public String getProtocClassPkg() {
        return protocClassPkg;
    }

    public void setProtocClassPkg(String protocClassPkg) {
        this.protocClassPkg = protocClassPkg;
    }
}
