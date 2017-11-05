package com.cloudtogo.plugins.proto.pojo.generator;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;

/**
 * proto pojo generator
 *
 * Created by cheney on 2017/10/6.
 */
@Mojo(name = "generate",defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class ProtoPojoGeneratorMojo extends AbstractMojo {

//    @Parameter(property = "project", required = true, readonly = true)
//    private MavenProject project;

    /**
     * .proto file or directory and source package config
     *
     * @parameter property="config"
     */
    @Parameter(property = "config", defaultValue = "src/main/resources/proto-pojo.xml", required = true)
    private File config;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        String projectDir = System.getProperty("user.dir") + File.separator;
        ProtoPojoGenerator protoPojoGenerator = new ProtoPojoGenerator(projectDir, config);
        try {
            protoPojoGenerator.generate();
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }
}
