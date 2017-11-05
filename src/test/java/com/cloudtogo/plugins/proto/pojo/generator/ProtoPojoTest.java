package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.ProtobufIDLProxy;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import com.cloudtogo.plugins.proto.pojo.generator.config.model.Proto;
import com.cloudtogo.plugins.proto.pojo.generator.config.model.ProtoConfig;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by cheney on 2017/10/26.
 */
public class ProtoPojoTest {

    @Test
    public void testGenerate() throws MojoExecutionException, IOException {
        String projectDir = System.getProperty("user.dir") + File.separator;
        String path = getClass().getResource("/proto-pojo-config.xml").getFile();
        ProtoPojoGenerator protoPojoGenerator = new ProtoPojoGenerator(projectDir, new File(path));
        protoPojoGenerator.generate();
        System.out.println("finished ...");
    }

    @Test
    public void testConfig() throws IOException {
        XStream xStream = new XStream(new DomDriver());
        XStream.setupDefaultSecurity(xStream);
        xStream.autodetectAnnotations(true);
        xStream.allowTypesByWildcard(new String[]{
                "com.cloudtogo.plugins.proto.pojo.generator.config.**"
        });
        xStream.alias("config", ProtoConfig.class);

        ProtoConfig protoList2 = (ProtoConfig) xStream.fromXML(getClass().getResourceAsStream("/proto-pojo-config.xml"));
        if (null == protoList2) {
            System.out.println(">>>>>>>> null");
        } else {
            // D:\workspace-github\cloudtogo\proto-pojo-generator-maven-plugin
            String projectDir = System.getProperty("user.dir");
            for (Proto proto : protoList2.getProtos()) {
                InputStream fis = new FileInputStream(projectDir + File.separator + proto.getFile());
                ProtoFile protoFile = ProtoParser.parseUtf8(ProtobufIDLProxy.DEFAULT_FILE_NAME, fis);
                for (TypeElement typeElement : protoFile.typeElements()) {
//                    System.out.println("message name: " + typeElement.name());
                    if (typeElement instanceof EnumElement) {
                        //
                    } else if (typeElement instanceof MessageElement) {
                        MessageElement messageElement = (MessageElement) typeElement;
                        for (FieldElement fieldElement : messageElement.fields()) {
                            DataType dataType = fieldElement.type();
                            String typeName;
                            if (dataType.kind() == DataType.Kind.MAP) {
//                                FieldType.MAP
                                typeName = ((DataType.MapType) dataType).keyType().toString();
                            } else {
                                typeName = fieldElement.type().toString();
                            }
                            if(dataType.kind() != DataType.Kind.MAP){
                                continue;
                            }

                            // label: FieldElement.Label.REPEATED,
                            // kind:  DataType.Kind.MAP: <keyType, valueType>
                            System.out.println("\tname = " + fieldElement.name()
                                    + "\tlabel = " + fieldElement.label()
                                    + "\tkind = " + dataType.kind().toString()
                                    + "\tDataType = " + fieldElement.type()
                                    + "\tFieldType = " + Constants.typeMapping.get(typeName));
                            if(dataType.kind() == DataType.Kind.MAP){
                                DataType.MapType mapType = (DataType.MapType) dataType;
                                String keyType = mapType.keyType().toString();
                                String valueType = mapType.valueType().toString();
                                System.out.println(">>>>> keyType = " + keyType + "; valueType = " + valueType);
                            }
                        }
                    } else {
                        System.err.println(typeElement.getClass().getName());
                    }
                }
                System.out.println("====================================");
            }
        }
    }
}
