package com.cloudtogo.plugins.proto.pojo.generator;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by cheney on 2017/10/24.
 */
public class ProtoPojoTest {

    @Test
    public void testJProtobuf() throws IOException, MojoExecutionException {
        String output = System.getProperty("user.dir");
//        output += "/src/test/gen";
        output += "/src/test/java";

        File file;

        ProtoModule monitorProtoModule = new ProtoModule();
        file = new File(getClass().getResource("/protobuf/monitor.proto").getFile());
        monitorProtoModule.setModel("com.monitor.model");
        monitorProtoModule.setProto(file);

        ProtoModule deployProtoModule = new ProtoModule();
        file = new File(getClass().getResource("/protobuf/deploy_svc.proto").getFile());
        deployProtoModule.setModel("com.deploy.model");
        deployProtoModule.setProto(file);

        ProtoModule typeProtoModule = new ProtoModule();
        file = new File(getClass().getResource("/protobuf/type.proto").getFile());
        typeProtoModule.setModel("com.types.model");
        typeProtoModule.setProto(file);

        ProtoPojoGenerator protoPojoGenerator = new ProtoPojoGenerator(output,
                new ProtoModule[]{monitorProtoModule, deployProtoModule, typeProtoModule},
                ".proto");
        protoPojoGenerator.generate();
    }
}
