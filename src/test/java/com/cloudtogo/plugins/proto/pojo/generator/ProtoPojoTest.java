package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.ProtobufIDLProxy;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by cheney on 2017/10/24.
 */
public class ProtoPojoTest {

    String extension = ".proto";
    private final String indent = "    ";

    @Test
    public void testJProtobuf() throws IOException {
        String output = System.getProperty("user.dir");
        output += "/src/test/gen";

        String f = getClass().getResource("/protobuf/monitor.proto").getFile();
        File file = new File(f);
        System.out.println(">>>>>>>>> " + file.getAbsolutePath());

        ProtoModule protoModule = new ProtoModule();
        protoModule.setPkg("com.monitor");
        protoModule.setProto(file);
        List<ProtoModule> protoModuleList = new ArrayList<>();
        protoModuleList.add(protoModule);
        this.generateSourceCode(protoModuleList, output);
    }

    private void generateSourceCode(List<ProtoModule> protoModules, String sourceDirectory) {
        IOFileFilter ioFileFilter = this.getFileFilter();

        List<File> protoFileList;
        for (ProtoModule protoModule : protoModules) {
            protoFileList = new ArrayList<>();
            if (protoModule.getProto().isDirectory()) {
                Collection<File> protoFiles = FileUtils.listFiles(protoModule.getProto(), ioFileFilter, TrueFileFilter.INSTANCE);
                protoFileList.addAll(protoFiles);
            } else {
                if (protoModule.getProto().getName().endsWith(extension)) {
                    protoFileList.add(protoModule.getProto());
                }
            }
            protoFileList.forEach(file -> {

                try {
                    InputStream fis = new FileInputStream(file);
                    ProtoFile protoFile = ProtoParser.parseUtf8(ProtobufIDLProxy.DEFAULT_FILE_NAME, fis);
                    List<TypeElement> typeElementList = protoFile.typeElements();
                    if (null == typeElementList || typeElementList.size() == 0) {
                        System.out.println("======== No message defined in .proto file[" + file.getName() + "] ========");
                    } else {
                        typeElementList.forEach(typeElement -> {
                            System.out.println(indent + indent + "generate java file: " + typeElement.name() + ".java");
                            ProtobufIDLProxy.CodeDependent codeDependent;
                            if (typeElement instanceof MessageElement) {
                                codeDependent = ProtoPojoGenerator.createCodeByType((MessageElement) typeElement, protoModule.getPkg(), protoModule.getFields());
                            } else {
                                codeDependent = ProtoPojoGenerator.createCodeByType((EnumElement) typeElement, protoModule.getPkg());
                            }
                            ProtoPojoGenerator.writeSourceCode(codeDependent, new File(sourceDirectory + File.separator));
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private IOFileFilter getFileFilter() {
        return new IOFileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(extension);
            }

            @Override
            public boolean accept(File file, String s) {
                return s.endsWith(extension);
            }
        };
    }
}
