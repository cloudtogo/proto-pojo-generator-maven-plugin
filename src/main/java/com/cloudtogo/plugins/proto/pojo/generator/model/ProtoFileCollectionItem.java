package com.cloudtogo.plugins.proto.pojo.generator.model;

import com.baidu.bjf.remoting.protobuf.ProtobufIDLProxy;
import com.baidu.jprotobuf.com.squareup.protoparser.ProtoFile;
import com.baidu.jprotobuf.com.squareup.protoparser.ProtoParser;
import com.cloudtogo.plugins.proto.pojo.generator.ProtoModule;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cheney on 2017/10/25.
 */
public class ProtoFileCollectionItem extends ProtoModule {

    private String name;

    private Map<File, ProtoFile> protoFileMap = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<File, ProtoFile> getProtoFileMap() {
        return protoFileMap;
    }

    public void setProtoFileMap(Map<File, ProtoFile> protoFileMap) {
        this.protoFileMap = protoFileMap;
    }

    public void addProtoFile(File file) throws IOException {
        InputStream fis = new FileInputStream(file);
        ProtoFile protoFile = ProtoParser.parseUtf8(ProtobufIDLProxy.DEFAULT_FILE_NAME, fis);
        this.protoFileMap.put(file, protoFile);
    }
}
