package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.ProtobufIDLProxy;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import com.cloudtogo.plugins.proto.pojo.generator.model.ProtoFileCollectionItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by cheney on 2017/10/6.
 */
public class ProtoPojoGenerator {

    private final String indent = "    ";

    private ProtoModule[] protoModules;
    // project.getBuild().getSourceDirectory()
    private String sourceDirectory;

    private String extension;

    private Log log;

    private List<ProtoFileCollectionItem> protoFileCollectionItemList = new ArrayList<>();

    public ProtoPojoGenerator(String sourceDirectory, ProtoModule[] protoModules, String extension) {
        this.sourceDirectory = sourceDirectory;
        this.protoModules = protoModules;
        this.extension = extension;
    }

    public void generate() throws MojoExecutionException, IOException {
        this.checkConfig();

        this.clearSourceDirectory();

        this.generateSourceCode();
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

    /**
     * check that the config is valid
     * @throws MojoExecutionException
     */
    private void checkConfig() throws MojoExecutionException, IOException {
        if (null == this.protoModules || this.protoModules.length == 0) {
            throw new MojoExecutionException("No proto module config.");
        }
        for (ProtoModule protoModule : this.protoModules) {
            if (StringUtils.isBlank(protoModule.getModel())) {
                throw new MojoExecutionException("pkg config is null.");
            }
        }

        IOFileFilter ioFileFilter = this.getFileFilter();

        ProtoFileCollectionItem protoFileCollectionItem;
        List<File> protoFileList;
        for (ProtoModule protoModule : this.protoModules) {
            protoFileCollectionItem = new ProtoFileCollectionItem();
            protoFileCollectionItem.setProto(protoModule.getProto());
            protoFileCollectionItem.setModel(protoModule.getModel());
            protoFileCollectionItem.setBuilder(protoModule.getBuilder());
            protoFileCollectionItem.setFields(protoModule.getFields());

            protoFileCollectionItem.setName(protoModule.getProto().getName());

            protoFileList = new ArrayList<>();
            if (protoModule.getProto().isDirectory()) {
                Collection<File> protoFiles = FileUtils.listFiles(protoModule.getProto(), ioFileFilter, TrueFileFilter.INSTANCE);
                protoFileList.addAll(protoFiles);
            } else {
                if (protoModule.getProto().getName().endsWith(extension)) {
                    protoFileList.add(protoModule.getProto());
                }
            }
            for (File file : protoFileList) {
                protoFileCollectionItem.addProtoFile(file);
            }
            this.protoFileCollectionItemList.add(protoFileCollectionItem);
        }
    }

    /**
     * clear all source directory configed
     */
    private void clearSourceDirectory() {
        getLog().info("");
        getLog().info("clear source directory:");
        List<String> sourceDirectoryList = new ArrayList<>();
        for (ProtoModule protoModule : this.protoModules) {
            if (StringUtils.isNotBlank(protoModule.getModel())) {
                sourceDirectoryList = this.clearPkg(sourceDirectoryList, protoModule.getModel());
            }
            if (StringUtils.isNotBlank(protoModule.getBuilder())) {
                sourceDirectoryList = this.clearPkg(sourceDirectoryList, protoModule.getBuilder());
            }
        }
    }

    /**
     * clear package directory
     * @param sourceDirectoryList directory list cleared
     * @param pkg package name
     * @return
     */
    private List<String> clearPkg(List<String> sourceDirectoryList, String pkg) {
        String pkgDirectory = this.sourceDirectory + File.separator + pkg.replace(".", File.separator);
        if (!sourceDirectoryList.contains(pkgDirectory)) {
            getLog().info(this.indent + pkgDirectory);
            sourceDirectoryList.add(pkgDirectory);
            File file = new File(pkgDirectory);
            if (file.exists()) {
                try {
                    FileUtils.deleteDirectory(file);
                } catch (IOException e) {
                    getLog().error(e.getMessage(), e);
                }
            }
            file.mkdirs();
        }
        return sourceDirectoryList;
    }

    /**
     * proto file filter
     * @return
     */
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

    private void generateSourceCode() throws MojoExecutionException {
        getLog().info("");
        getLog().info("generate source code:");
        for (ProtoFileCollectionItem protoFileCollectionItem : this.protoFileCollectionItemList) {
            getLog().info(indent + "deal .proto file: " + protoFileCollectionItem.getProto().getAbsolutePath());
            if (StringUtils.isBlank(protoFileCollectionItem.getModel())) {
                getLog().info(getIndent(2) + "model package: null");
            } else {
                getLog().info(getIndent(2) + "model package: " + protoFileCollectionItem.getModel());
                this.generateSourceModelCode(protoFileCollectionItem);
            }
            getLog().info(getIndent(2) + "builder package: " + protoFileCollectionItem.getBuilder());
        }
    }

    /**
     * generate model source code
     * @param protoFileCollectionItem
     */
    private void generateSourceModelCode(ProtoFileCollectionItem protoFileCollectionItem) throws MojoExecutionException {
        if (null == protoFileCollectionItem.getProtoFileMap() || protoFileCollectionItem.getProtoFileMap().size() == 0) {
            getLog().info(getIndent(3) + "no profile needs to generate model code");
        } else {
            Set<Map.Entry<File, ProtoFile>> set = protoFileCollectionItem.getProtoFileMap().entrySet();
            for (Map.Entry<File, ProtoFile> entry : set) {
                List<TypeElement> typeElementList = entry.getValue().typeElements();
                if (null == typeElementList || typeElementList.size() == 0) {
                    getLog().error("======== No message defined in .proto file[" + entry.getKey().getName() + "] ========");
                } else {
                    for (TypeElement typeElement : typeElementList) {
                        getLog().info(getIndent(3) + "generate java file: " + typeElement.name() + ".java");
                        ProtobufIDLProxy.CodeDependent codeDependent;
                        if (typeElement instanceof MessageElement) {
//                            codeDependent = this.createCodeByType((MessageElement) typeElement,protoFileCollectionItem.getModel(), protoFileCollectionItem.getFields());
                            codeDependent = this.createMessageElementModel(protoFileCollectionItem, entry.getValue(), (MessageElement) typeElement);
                        } else {
//                            codeDependent = this.createCodeByType((EnumElement) typeElement, protoFileCollectionItem.getModel());
                            codeDependent = ProtobufIDLProxy.createCodeByType((EnumElement) typeElement, true, protoFileCollectionItem.getModel());
                        }
//                        ProtoPojoGenerator.writeSourceCode(codeDependent, new File(this.project.getBuild().getSourceDirectory() + File.separator));
                        ProtobufIDLProxy.writeSourceCode(codeDependent, new File(this.sourceDirectory + File.separator));
                    }
                }
            }
        }
    }

    private ProtobufIDLProxy.CodeDependent createMessageElementModel(ProtoFileCollectionItem protoFileCollectionItem, ProtoFile protoFile, MessageElement messageElement) throws MojoExecutionException {
        String className = messageElement.name();

        StringBuilder importBuilder = this.createMessageElementModelImport(messageElement);

        StringBuilder code = new StringBuilder();
        code.append("package ").append(protoFileCollectionItem.getModel()).append(ProtobufIDLProxy.CODE_END);
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        code.append(importBuilder);

        code.append("public class ").append(className).append(" {");
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        code = createFiedlsCode(code, messageElement, protoFileCollectionItem.getFields());
        code.append("}");
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        ProtobufIDLProxy.CodeDependent cd = new ProtobufIDLProxy.CodeDependent();
        cd.setName(className);
        cd.setPkg(protoFileCollectionItem.getModel());
        cd.setCode(code.toString());
        return cd;
    }

    private StringBuilder createMessageElementModelImport(MessageElement messageElement) throws MojoExecutionException {
        StringBuilder code = new StringBuilder();
        List<FieldElement> fields = messageElement.fields();
        boolean existsImport = false;
        for (FieldElement field : fields) {
            String javaType = this.getjavaType(field);
            if (StringUtils.isNotBlank(javaType) && javaType.contains(".")) {
                existsImport = true;
                String protoPkgName = javaType.substring(0, javaType.lastIndexOf("."));
                boolean existsProto = false;
                for (ProtoFileCollectionItem protoFileCollectionItem : this.protoFileCollectionItemList) {
                    Set<Map.Entry<File, ProtoFile>> set = protoFileCollectionItem.getProtoFileMap().entrySet();
                    for (Map.Entry<File, ProtoFile> entry : set) {
                        if (protoPkgName.equals(entry.getValue().packageName())) {
                            existsProto = true;
                            // import org.hibernate.validator.constraints.NotEmpty;
                            code.append("import ")
                                    .append(protoFileCollectionItem.getModel())
                                    .append(".")
                                    .append(javaType.substring(javaType.lastIndexOf(".") + 1))
                                    .append(ProtobufIDLProxy.CODE_END);
                        }
                    }
                }
                if(existsProto == false){
                    throw new MojoExecutionException("Need proto file whose package is [" + protoPkgName + "]");
                }
            }
        }
        if (existsImport) {
            code.append(ProtobufIDLProxy.CodeDependent.NewLine);
        }
        return code;
    }

    private String getSimpleJavaType(FieldElement fieldElement) {
        String javaType = this.getjavaType(fieldElement);
        if (StringUtils.isNotBlank(javaType) && javaType.contains(".")) {
            return javaType.substring(javaType.lastIndexOf(".") + 1);
        }
        return javaType;
    }

    private String getjavaType(FieldElement fieldElement) {
        DataType dataType = fieldElement.type();
        String typeName;
        if (dataType.kind() == DataType.Kind.MAP) {
            typeName = ((DataType.MapType) dataType).keyType().toString();
        } else {
            typeName = fieldElement.type().toString();
        }

        com.baidu.bjf.remoting.protobuf.FieldType fType = ProtobufIDLProxy.typeMapping.get(typeName);
        String javaType;
        if (null == fType) {
            javaType = fieldElement.type().toString();
        } else {
            javaType = fType.getJavaType();
        }
        return javaType;
    }

    /**
     * message element
     *
     * @param messageElement MessageElement instance
     *
     *
     * @param serializeAndDeserializeFields need to serialize or deserialize field collection
     *
     * @return ProtobufIDLProxy.CodeDependent
     */
/*    private ProtobufIDLProxy.CodeDependent createCodeByType(MessageElement messageElement, String pkg, List<SerializeAndDeserialize> serializeAndDeserializeFields){
        String simpleName = messageElement.name();
        StringBuilder code = new StringBuilder();
        code.append("package ").append(pkg).append(ProtobufIDLProxy.CODE_END);
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        code.append("public class ").append(simpleName).append(" {");
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        code = createFiedlsCode(code, messageElement, serializeAndDeserializeFields);
        code.append("}");
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        ProtobufIDLProxy.CodeDependent cd = new ProtobufIDLProxy.CodeDependent();
        cd.setName(simpleName);
        cd.setPkg(pkg);
        cd.setCode(code.toString());
        return cd;
    }*/

    private StringBuilder createFiedlsCode(StringBuilder code, MessageElement messageElement, List<SerializeAndDeserialize> serializeAndDeserializeFields) {
        StringBuilder fieldCode = new StringBuilder();
        StringBuilder getSetCode = new StringBuilder();

        List<FieldElement> fields = messageElement.fields();
        for (FieldElement field : fields) {
            DataType dataType = field.type();
            String javaType = this.getSimpleJavaType(field);

            // fix java type for map type
            if (dataType.kind() == DataType.Kind.MAP) {
                DataType.MapType mapType = (DataType.MapType) dataType;
                String keyType = mapType.keyType().toString();
                FieldType subType = ProtobufIDLProxy.typeMapping.get(keyType);
                String keyJavaType;
                if (subType == null) {
                    keyJavaType = keyType;
                } else {
                    keyJavaType = subType.getJavaType();
                }

                String valueType = mapType.valueType().toString();
                subType = ProtobufIDLProxy.typeMapping.get(valueType);
                String valueJavaType;
                if (subType == null) {
                    valueJavaType = valueType;
                } else {
                    valueJavaType = subType.getJavaType();
                }
                javaType = Map.class.getName() + "<" + keyJavaType + ", " + valueJavaType + ">";
            }

            // check if repeated type
            if (FieldElement.Label.REPEATED == field.label()) {
                javaType = List.class.getName() + "<" + javaType + ">";
            }

            // define field
            if (null != serializeAndDeserializeFields) {
                for (SerializeAndDeserialize serializeAndDeserialize : serializeAndDeserializeFields) {
                    if (messageElement.name().equals(serializeAndDeserialize.getMessage())
                            && field.name().equalsIgnoreCase(serializeAndDeserialize.getField())) {
                        if (StringUtils.isNotBlank(serializeAndDeserialize.getSerialize())) {
                            fieldCode.append(ProtobufIDLProxy.CodeDependent.Indent)
                                    .append(String.format("@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = %s.class)", serializeAndDeserialize.getSerialize()))
                                    .append(ProtobufIDLProxy.CodeDependent.NewLine);
                        }
                        if (StringUtils.isNotBlank(serializeAndDeserialize.getDeserialize())) {
                            fieldCode.append(ProtobufIDLProxy.CodeDependent.Indent)
                                    .append(String.format("@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = %s.class)", serializeAndDeserialize.getDeserialize()))
                                    .append(ProtobufIDLProxy.CodeDependent.NewLine);
                        }
                    }
                }
            }
            fieldCode.append(ProtobufIDLProxy.CodeDependent.Indent);
            fieldCode.append("private ").append(javaType);
            fieldCode.append(" ").append(field.name());

            // check if has default
            OptionElement defaultOption = OptionElement.findByName(field.options(), "default");
            if (defaultOption != null) {
                fieldCode.append("=");
                Object defaultValue = defaultOption.value();
                // if is enum type
                if (defaultOption.kind() == OptionElement.Kind.ENUM) {
                    fieldCode.append(javaType).append(".").append(defaultValue);
                } else if (defaultOption.kind() == OptionElement.Kind.STRING) {
                    fieldCode.append("\"").append(defaultValue).append("\"");
                } else {
                    fieldCode.append(String.valueOf(defaultValue));
                }
            }
            fieldCode.append(ProtobufIDLProxy.CODE_END);
            fieldCode.append(ProtobufIDLProxy.CodeDependent.NewLine);

            // getter
            getSetCode.append(ProtobufIDLProxy.CodeDependent.Indent);
            getSetCode.append("public ").append(javaType).append(" get").append(StringUtils.capitalize(field.name()));
            getSetCode.append("() {");
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            getSetCode.append(ProtobufIDLProxy.CodeDependent.Indent).append(ProtobufIDLProxy.CodeDependent.Indent);
            getSetCode.append("return " + field.name() + ";");
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            getSetCode.append(ProtobufIDLProxy.CodeDependent.Indent);
            getSetCode.append("}");
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            // setter
            getSetCode.append(ProtobufIDLProxy.CodeDependent.Indent);
            getSetCode.append("public void ").append(" set").append(StringUtils.capitalize(field.name()));
            getSetCode.append("(").append(javaType).append(" ").append(field.name()).append(") {");
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            getSetCode.append(ProtobufIDLProxy.CodeDependent.Indent).append(ProtobufIDLProxy.CodeDependent.Indent);
            getSetCode.append("this." + field.name() + " = " + field.name() + ";");
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            getSetCode.append(ProtobufIDLProxy.CodeDependent.Indent);
            getSetCode.append("}");
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
            getSetCode.append(ProtobufIDLProxy.CodeDependent.NewLine);
        }
        code.append(fieldCode);
        code.append(getSetCode);
        return code;
    }

    private String getIndent(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(this.indent);
        }
        return builder.toString();
    }

}
