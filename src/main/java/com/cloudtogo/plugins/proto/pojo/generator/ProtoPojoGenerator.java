package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.jprotobuf.com.squareup.protoparser.*;
import com.cloudtogo.plugins.proto.pojo.generator.config.model.Proto;
import com.cloudtogo.plugins.proto.pojo.generator.config.model.ProtoConfig;
import com.cloudtogo.plugins.proto.pojo.generator.config.model.SpecialField;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by cheney on 2017/10/29.
 */
public class ProtoPojoGenerator {

    // ${project}
    private String projectDir;
    // ${project}/src/main/java
    private String sourceDirectory;
    private ProtoConfig protoConfig;

    private static final String extension = ".proto";

    private Log log;

    private List<ProtoMessageMap> protoMessageMapList = new ArrayList<>();
    private List<ProtoMessageClass> protoMessageClassList = new ArrayList<>();
    private List<ProtoEnumClass> protoEnumClassList = new ArrayList<>();

    public ProtoPojoGenerator(String projectDir, File configFile) {
        XStream xStream = new XStream(new DomDriver());
        XStream.setupDefaultSecurity(xStream);
        xStream.autodetectAnnotations(true);
        xStream.allowTypesByWildcard(new String[]{
                "com.cloudtogo.plugins.proto.pojo.generator.config.**"
        });
        xStream.alias("config", ProtoConfig.class);

        this.protoConfig = (ProtoConfig) xStream.fromXML(configFile);
        this.projectDir = projectDir;
        this.sourceDirectory = this.projectDir + "/src/main/java";
//        this.sourceDirectory = this.projectDir + "/src/test/java";
    }

    public Log getLog() {
        if (log == null) {
            log = new SystemStreamLog();
        }
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public void generate() throws MojoExecutionException, IOException {
        this.checkConfig();
        this.convertConfig();

        this.cleanSourceCode();
        this.writeSourceCode();
    }

    private void checkConfig() throws MojoExecutionException {
        if (null == this.protoConfig
                || null == this.protoConfig.getProtos() || this.protoConfig.getProtos().size() == 0) {
            throw new MojoExecutionException("config is null.");
        }
    }

    private void convertConfig() throws MojoExecutionException, IOException {
        for (Proto proto : this.protoConfig.getProtos()) {
            if (StringUtils.isBlank(proto.getFile()) || !proto.getFile().endsWith(this.extension)) {
                throw new MojoExecutionException(String.format("[%s] is not a proto file.", proto.getFile()));
            }
            String path = this.projectDir + File.separator + proto.getFile();
            ProtoFile protoFile = ProtoParser.parseUtf8(FilenameUtils.getBaseName(path), new FileInputStream(path));
            if (null != protoFile.typeElements()) {
                for (TypeElement typeElement : protoFile.typeElements()) {
                    ProtoMessageMap protoMessageMap = new ProtoMessageMap();
                    protoMessageMap.setFile(proto.getFile());
                    protoMessageMap.setPkg(protoFile.packageName());
                    protoMessageMap.setName(typeElement.name());
                    protoMessageMap.setPojoClassName(this.getPojoClassName(typeElement));
                    protoMessageMap.setPojoPkg(proto.getPojo());
                    if (StringUtils.isNotBlank(proto.getBuilder())) {
                        protoMessageMap.setBuilderClassName(this.getBuilderClassName(typeElement));
                        protoMessageMap.setBuilderPkg(proto.getBuilder());
                    }
                    String protoClassName = this.getProtocClassName(protoFile.filePath(), null == proto.getProtoc() ? null : proto.getProtoc().getClassName());
                    String protoClassPkg = this.getProtocClassPkg(protoFile.packageName(), null == proto.getProtoc() ? null : proto.getProtoc().getPkg());
                    protoMessageMap.setProtocClassName(protoClassName);
                    protoMessageMap.setProtocClassPkg(protoClassPkg);
                    protoMessageMapList.add(protoMessageMap);
                }
            }
        }
        for (Proto proto : this.protoConfig.getProtos()) {
            String path = this.projectDir + File.separator + proto.getFile();
            ProtoFile protoFile = ProtoParser.parseUtf8(FilenameUtils.getBaseName(path), new FileInputStream(path));
            if (null != protoFile.typeElements()) {
                for (TypeElement typeElement : protoFile.typeElements()) {
                    if (typeElement instanceof EnumElement) {
                        ProtoEnumClass protoEnumClass = new ProtoEnumClass();
                        protoEnumClass.setFile(proto.getFile());
                        protoEnumClass.setPkg(protoFile.packageName());
                        protoEnumClass.setName(typeElement.name());
                        protoEnumClass.setPojoPkg(proto.getPojo());
                        protoEnumClass.setType((EnumElement) typeElement);
                        protoEnumClassList.add(protoEnumClass);
                    } else if (typeElement instanceof MessageElement) {
                        ProtoMessageClass protoMessageClass = this.toProtoMessageClass(proto, protoFile, (MessageElement) typeElement);
                        protoMessageClassList.add(protoMessageClass);
                    }
                }
            }
        }
    }

    private ProtoMessageClass toProtoMessageClass(Proto proto, ProtoFile protoFile, MessageElement messageElement) {
        ProtoMessageClass protoMessageClass = new ProtoMessageClass();
        protoMessageClass.setFile(proto.getFile());
        protoMessageClass.setPkg(protoFile.packageName());
        protoMessageClass.setName(messageElement.name());
        protoMessageClass.setPojoClassName(this.getPojoClassName(messageElement));
        protoMessageClass.setPojoPkg(proto.getPojo());
        if (StringUtils.isNotBlank(proto.getBuilder())) {
            protoMessageClass.setBuilderClassName(this.getBuilderClassName(messageElement));
            protoMessageClass.setBuilderPkg(proto.getBuilder());
        }
        String protoClassName = this.getProtocClassName(protoFile.filePath(), null == proto.getProtoc() ? null : proto.getProtoc().getClassName());
        String protoClassPkg = this.getProtocClassPkg(protoFile.packageName(), null == proto.getProtoc() ? null : proto.getProtoc().getPkg());
        protoMessageClass.setProtocClassName(protoClassName);
        protoMessageClass.setProtocClassPkg(protoClassPkg);

        List<String> dependencies = protoFile.dependencies();
        List<ProtoMessageMap> protoMessageMapListPerhaps = new ArrayList<>();
        if (null != dependencies) {
            dependencies.forEach(s -> this.protoMessageMapList.forEach(protoMessageMap -> {
                if (protoMessageMap.getFile().endsWith(s)) {
                    protoMessageMapListPerhaps.add(protoMessageMap);
                }
            }));
        }
        // field
        List<ProtoMessageField> protoMessageFieldList = new ArrayList<>();
        for (FieldElement fieldElement : messageElement.fields()) {
            SpecialField curSpecialField = null;
            if (null != proto.getFields()) {
                for (SpecialField specialField : proto.getFields()) {
                    if (specialField.getMessage().equals(messageElement.name()) && specialField.getField().equals(fieldElement.name())) {
                        curSpecialField = specialField;
                        break;
                    }
                }
            }
            protoMessageFieldList.add(new ProtoMessageField(protoMessageClass, protoFile, fieldElement, protoMessageMapListPerhaps, curSpecialField, getLog()));
        }
        protoMessageClass.setFieldList(protoMessageFieldList);
        return protoMessageClass;
    }

    private String getPojoClassName(TypeElement typeElement) {
        return typeElement.name();
    }

    private String getBuilderClassName(TypeElement typeElement) {
        return typeElement.name() + "Builder";
    }

    private String getProtocClassName(String protoFileName, String def) {
        if (StringUtils.isNotBlank(def)) {
            return def;
        }
        StringBuilder builder = new StringBuilder();
        for (String str : protoFileName.split("_")) {
            builder.append(StringUtils.capitalize(str));
        }
        String protocClassName = builder.toString();
        return protocClassName;
    }

    private String getProtocClassPkg(String pkgInProtoFile, String configed) {
        if (StringUtils.isBlank(configed)) {
            return pkgInProtoFile;
        }
        return pkgInProtoFile;
    }

    private void cleanSourceCode() {
        getLog().info("");
        getLog().info("clear source directory:");
        this.protoConfig.getProtos().forEach(proto -> {
            this.cleanPackage(proto.getPojo());
            this.cleanPackage(proto.getBuilder());
        });
    }

    private void cleanPackage(String pkg) {
        if (StringUtils.isBlank(pkg)) {
            return;
        }
        getLog().info(Constants.Indent(1) + "package: " + pkg);
        String directory = this.sourceDirectory + File.separator + pkg.replace(".", File.separator);
        File file = new File(directory);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeSourceCode() throws IOException {
        getLog().info("");
        getLog().info("generate source code:");
        for (Proto proto : this.protoConfig.getProtos()) {
            getLog().info(Constants.Indent(1) + "deal file: " + proto.getFile());
            for (ProtoEnumClass protoEnumClass : this.protoEnumClassList) {
                if (protoEnumClass.getFile().equals(proto.getFile())) {
                    getLog().info(Constants.Indent(2) + "enum " + protoEnumClass.getName());
                    getLog().info(Constants.Indent(3) + "generate enum class: " + protoEnumClass.getPojoClassName() + ".java");
                    this.writeEnumCode(protoEnumClass);
                }
            }
            for (ProtoMessageClass protoMessageClass : this.protoMessageClassList) {
                if (proto.getFile().equals(protoMessageClass.getFile())) {
                    getLog().info(Constants.Indent(2) + "message " + protoMessageClass.getName());
                    getLog().info(Constants.Indent(3) + "generate pojo class: " + protoMessageClass.getPojoClassName() + ".java");
                    this.writePojoCode(protoMessageClass);

                    if (StringUtils.isNotBlank(protoMessageClass.getBuilderClassName())
                            && StringUtils.isNotBlank(protoMessageClass.getBuilderPkg())) {
                        getLog().info(Constants.Indent(3) + "generate builder class: " + protoMessageClass.getBuilderClassName() + ".java");
                        this.writeBuilderCode(protoMessageClass);
                    }
                }
            }
        }
    }

    private void writeEnumCode(ProtoEnumClass protoEnumClass) throws IOException {
        StringBuilder code = new StringBuilder();
        code.append(String.format("package %s;", protoEnumClass.getPojoPkg()))
                .append(Constants.NewLine).append(Constants.NewLine);
        code.append(String.format("public enum %s {", protoEnumClass.getPojoClassName())).append(Constants.NewLine);

        Iterator<EnumConstantElement> iter = protoEnumClass.getType().constants().iterator();
        while (iter.hasNext()) {
            EnumConstantElement value = iter.next();
            String name = value.name();
            int tag = value.tag();
            if (iter.hasNext()) {
                code.append(Constants.Indent(1)).append(String.format("%s(%d),", name, tag))
                        .append(Constants.NewLine);
            } else {
                code.append(Constants.Indent(1)).append(String.format("%s(%d);", name, tag))
                        .append(Constants.NewLine).append(Constants.NewLine);
            }
        }

        code.append(Constants.Indent(1)).append("private final int value;")
                .append(Constants.NewLine).append(Constants.NewLine);

        code.append(Constants.Indent(1)).append(String.format("%s(int value) {", protoEnumClass.getPojoClassName())).append(Constants.NewLine);
        code.append(Constants.Indent(2)).append("this.value = value;").append(Constants.NewLine);
        code.append(Constants.Indent(1)).append("}").append(Constants.NewLine).append(Constants.NewLine);

        code.append(Constants.Indent(1)).append("public int value() {").append(Constants.NewLine);
        code.append(Constants.Indent(2)).append("return value;").append(Constants.NewLine);
        code.append(Constants.Indent(1)).append("}").append(Constants.NewLine).append(Constants.NewLine);

        code.append(Constants.Indent(1)).append(String.format("public static %s valueOf(int value) {", protoEnumClass.getPojoClassName())).append(Constants.NewLine);
        code.append(Constants.Indent(2)).append("switch (value) {").append(Constants.NewLine);
        iter = protoEnumClass.getType().constants().iterator();
        while (iter.hasNext()) {
            EnumConstantElement value = iter.next();
            String name = value.name();
            int tag = value.tag();
            code.append(Constants.Indent(3)).append(String.format("case %d: return %s;", tag, name)).append(Constants.NewLine);
        }
        code.append(Constants.Indent(3)).append("default: return null;").append(Constants.NewLine);
        code.append(Constants.Indent(2)).append("}").append(Constants.NewLine);
        code.append(Constants.Indent(1)).append("}").append(Constants.NewLine).append(Constants.NewLine);

        code.append("}");
        this.writeToFile(this.sourceDirectory + File.separator + protoEnumClass.getPojoPkg().replace(".", File.separator) + File.separator,
                protoEnumClass.getPojoClassName() + ".java",
                code);
    }

    private void writePojoCode(ProtoMessageClass protoMessageClass) throws IOException {
        StringBuilder code = new StringBuilder();

        StringBuilder fieldCode = new StringBuilder();
        StringBuilder getSetCode = new StringBuilder();
        List<String> importList = new ArrayList<>();

        protoMessageClass.getFieldList().forEach(protoMessageField -> {
            fieldCode.append(protoMessageField.getSchema());
            getSetCode.append(protoMessageField.createGetMethodCode()).append(protoMessageField.createSetMethodCode());
            protoMessageField.getPojoDependencyList().forEach(s -> {
                if (!importList.contains(s)) importList.add(s);
            });
        });

        // package
        code.append(String.format("package %s;", protoMessageClass.getPojoPkg()))
                .append(Constants.NewLine).append(Constants.NewLine);
        // import: dependency
        importList.forEach(s -> code.append(String.format("import %s;", s)).append(Constants.NewLine));
        if (importList.size() > 0) code.append(Constants.NewLine);

        code.append(String.format("public class %s {", protoMessageClass.getPojoClassName()))
                .append(Constants.NewLine).append(Constants.NewLine);
        // field
        code.append(fieldCode).append(Constants.NewLine);
        // field get/set
        code.append(getSetCode);
        code.append("}");

        this.writeToFile(this.sourceDirectory + File.separator + protoMessageClass.getPojoPkg().replace(".", File.separator) + File.separator,
                protoMessageClass.getPojoClassName() + ".java",
                code);
    }

    private void writeBuilderCode(ProtoMessageClass protoMessageClass) throws IOException {
        StringBuilder code = new StringBuilder();
        // package
        code.append(String.format("package %s;", protoMessageClass.getBuilderPkg()))
                .append(Constants.NewLine).append(Constants.NewLine);
        // import: dependency
        code.append(String.format("import %s.%s;", protoMessageClass.getPojoPkg(), protoMessageClass.getPojoClassName())).append(Constants.NewLine);
        code.append(String.format("import %s.%s;", protoMessageClass.getProtocClassPkg(), protoMessageClass.getProtocClassName())).append(Constants.NewLine);
        List<String> importList = new ArrayList<>();
        protoMessageClass.getFieldList().forEach(protoMessageField -> {
            protoMessageField.getBuilderDependencyList().forEach(s -> {
                if (!importList.contains(s)) {
                    importList.add(s);
                    code.append(String.format("import %s;", s)).append(Constants.NewLine);
                }
            });
        });
        code.append(Constants.NewLine);

        code.append(String.format("public class %s {", protoMessageClass.getBuilderClassName()))
                .append(Constants.NewLine).append(Constants.NewLine);
        // build
        code.append(this.createBuilderBuildCode(protoMessageClass));
        // unbuild
        code.append(this.createUnbuilderBuildCode(protoMessageClass));
        code.append("}");

        this.writeToFile(this.sourceDirectory + File.separator + protoMessageClass.getBuilderPkg().replace(".", File.separator) + File.separator,
                protoMessageClass.getBuilderClassName() + ".java",
                code);
    }

    private StringBuilder createBuilderBuildCode(ProtoMessageClass protoMessageClass) {
        String val = "builder", arg = "arg";
        // Type.Cluster
        String protoType = protoMessageClass.getProtocClassName() + "." + StringUtils.capitalize(protoMessageClass.getName());
        StringBuilder code = new StringBuilder();
        // public static Type.Cluster.Builder build(Cluster cluster) {
        code.append(Constants.Indent(1))
                .append(String.format("public static %s.Builder build(%s %s) {",
                        protoType, protoMessageClass.getPojoClassName(), arg))
                .append(Constants.NewLine);
        // DeploySvc.ClusterRequest.Builder builder = DeploySvc.ClusterRequest.newBuilder();
        code.append(Constants.Indent(2)).append(String.format("%s.Builder %s = %s.newBuilder();", protoType, val, protoType)).append(Constants.NewLine);
        code.append(Constants.Indent(2)).append(String.format("if (null == %s) {", arg)).append(Constants.NewLine);
        code.append(Constants.Indent(3)).append(String.format("return %s;", val)).append(Constants.NewLine);
        code.append(Constants.Indent(2)).append("}").append(Constants.NewLine);
        protoMessageClass.getFieldList().forEach(protoMessageField ->
                code.append(protoMessageField.createBuildCode(val, arg))
        );
        code.append(Constants.Indent(2)).append(String.format("return %s;", val)).append(Constants.NewLine);
        code.append(Constants.Indent(1)).append("}").append(Constants.NewLine);
        return code;
    }

    private StringBuilder createUnbuilderBuildCode(ProtoMessageClass protoMessageClass) {
        String val = StringUtils.uncapitalize(protoMessageClass.getName()), arg = "arg";
        StringBuilder code = new StringBuilder();
        // Type.Cluster
        String protoType = protoMessageClass.getProtocClassName() + "." + StringUtils.capitalize(protoMessageClass.getName());
        // public static Cluster unbuild(Type.Cluster arg){
        code.append(Constants.Indent(1))
                .append(String.format("public static %s unbuild(%s %s) {",
                        protoMessageClass.getPojoClassName(), protoType, arg))
                .append(Constants.NewLine);
        // if(null == arg) return null;
        code.append(Constants.Indent(2)).append(String.format("if (null == %s) {", arg)).append(Constants.NewLine);
        code.append(Constants.Indent(3)).append("return null;").append(Constants.NewLine);
        code.append(Constants.Indent(2)).append("}").append(Constants.NewLine);
        code.append(Constants.Indent(2)).append(String.format("%s %s = new %s();", protoMessageClass.getPojoClassName(), val, protoMessageClass.getPojoClassName())).append(Constants.NewLine);
        protoMessageClass.getFieldList().forEach(protoMessageField ->
                code.append(protoMessageField.createUnbuildCode(val, arg))
        );
        code.append(Constants.Indent(2)).append(String.format("return %s;", val)).append(Constants.NewLine);
        code.append(Constants.Indent(1)).append("}").append(Constants.NewLine);
        return code;
    }

    private void writeToFile(String path, String name, StringBuilder code) throws IOException {
        File file = new File(path);
        file.mkdirs();
        FileUtils.write(new File(path + name), code, "UTF-8");
    }

}
