package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.ProtobufIDLProxy;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * proto pojo generator
 *
 * Created by cheney on 2017/10/6.
 */
@Mojo(name = "generate",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProtoPojoGeneratorMojo extends AbstractMojo {

    @Parameter(property = "project", required = true, readonly = true)
    private MavenProject project;

    /**
     * .proto file or directory and source package config
     *
     * @parameter property="protoModules"
     */
    @Parameter(required = true)
    private ProtoModule[] protoModules;

    /**
     * Default extension for protobuf files
     *
     * @parameter property="extension" default-value=".proto"
     */
    @Parameter(property = "extension", defaultValue = ".proto", required = true)
    private String extension;

    private final String indent = "    ";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        this.checkConfig();

        this.clearSourceDirectory();

        this.generateSourceCode();
    }

    private void checkConfig() throws MojoExecutionException {
        if (null == this.protoModules || this.protoModules.length == 0) {
            throw new MojoExecutionException("No proto module config.");
        }
        for (ProtoModule protoModule : this.protoModules) {
            if (StringUtils.isBlank(protoModule.getPkg())) {
                throw new MojoExecutionException("pkg config is null.");
            }
        }
    }

    private void clearSourceDirectory() {
        getLog().info("");
        getLog().info("clear source directory:");
        List<String> sourceDirectoryList = new ArrayList<>();
        String sourceDirectory;
        File file;
        for (ProtoModule protoModule : this.protoModules) {
            sourceDirectory = this.project.getBuild().getSourceDirectory() + File.separator + protoModule.getPkg().replace(".", File.separator);
            if (!sourceDirectoryList.contains(sourceDirectory)) {
                getLog().info(this.indent + sourceDirectory);
                sourceDirectoryList.add(sourceDirectory);
                file = new File(sourceDirectory);
                if (file.exists()) {
                    try {
                        FileUtils.deleteDirectory(file);
                    } catch (IOException e) {
                        getLog().error(e.getMessage(), e);
                    }
                }
                file.mkdirs();
            }
        }
    }

    private void generateSourceCode() {
        getLog().info("");
        getLog().info("generate source code:");
        IOFileFilter ioFileFilter = this.getFileFilter();

        List<File> protoFileList;
        for (ProtoModule protoModule : this.protoModules) {
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
                getLog().info(indent + "deal .proto file: " + file.getAbsolutePath());
                getLog().info(indent + indent + "package: " + protoModule.getPkg());

                try {
                    InputStream fis = new FileInputStream(file);
                    ProtoFile protoFile = ProtoParser.parseUtf8(ProtobufIDLProxy.DEFAULT_FILE_NAME, fis);
                    List<TypeElement> typeElementList = protoFile.typeElements();
                    if (null == typeElementList || typeElementList.size() == 0) {
                        getLog().error("======== No message defined in .proto file[" + file.getName() + "] ========");
                    } else {
                        typeElementList.forEach(typeElement -> {
                            getLog().info(indent + indent + "generate java file: " + typeElement.name() + ".java");
                            ProtobufIDLProxy.CodeDependent codeDependent;
                            if (typeElement instanceof MessageElement) {
                                codeDependent = ProtoPojoGenerator.createCodeByType((MessageElement) typeElement, protoModule.getPkg());
                            } else {
                                codeDependent = ProtoPojoGenerator.createCodeByType((EnumElement) typeElement, protoModule.getPkg());
                            }
                            ProtoPojoGenerator.writeSourceCode(codeDependent, new File(this.project.getBuild().getSourceDirectory() + File.separator));
                        });
                    }
                } catch (IOException e) {
                    getLog().info(e.getMessage(), e);
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
