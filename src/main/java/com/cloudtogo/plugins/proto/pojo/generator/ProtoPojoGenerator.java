package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.bjf.remoting.protobuf.ProtobufIDLProxy;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by cheney on 2017/10/6.
 */
public class ProtoPojoGenerator {


    /**
     * enum element
     *
     * @param enumElement EnumElement instance
     *
     * @param pkg package name
     *
     * @return ProtobufIDLProxy.CodeDependent
     */
    public static ProtobufIDLProxy.CodeDependent createCodeByType(EnumElement enumElement, String pkg) {
        return ProtobufIDLProxy.createCodeByType(enumElement, true, pkg);
    }

    /**
     * message element
     *
     * @param messageElement MessageElement instance
     *
     * @param pkg package name
     *
     * @return ProtobufIDLProxy.CodeDependent
     */
    public static ProtobufIDLProxy.CodeDependent createCodeByType(MessageElement messageElement, String pkg){
        String simpleName = messageElement.name();
        StringBuilder code = new StringBuilder();
        code.append("package ").append(pkg).append(ProtobufIDLProxy.CODE_END);
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        code.append("public class ").append(simpleName).append(" {");
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        code = createFiedlsCode(code, messageElement);
        code.append("}");
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);

        ProtobufIDLProxy.CodeDependent cd = new ProtobufIDLProxy.CodeDependent();
        cd.setName(simpleName);
        cd.setPkg(pkg);
        cd.setCode(code.toString());
        return cd;
    }

    private static StringBuilder createFiedlsCode(StringBuilder code, MessageElement messageElement){
        StringBuilder fieldCode = new StringBuilder();
        StringBuilder getSetCode = new StringBuilder();

        List<FieldElement> fields = messageElement.fields();
        for (FieldElement field : fields) {
            DataType dataType = field.type();
            String typeName;
            if (dataType.kind() == DataType.Kind.MAP) {
                typeName = ((DataType.MapType) dataType).keyType().toString();
            } else {
                typeName = field.type().toString();
            }

            com.baidu.bjf.remoting.protobuf.FieldType fType = ProtobufIDLProxy.typeMapping.get(typeName);
            String javaType;
            if(null == fType){
                javaType = field.type().toString();
            } else {
                javaType = fType.getJavaType();
            }

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
        code.append(ProtobufIDLProxy.CodeDependent.NewLine);
        code.append(getSetCode);
        return code;
    }

    /**
     * generate source file
     *
     * @param cd ProtobufIDLProxy.CodeDependent instance
     *
     * @param sourceOutputDir java file directory
     */
    public static void writeSourceCode(ProtobufIDLProxy.CodeDependent cd, File sourceOutputDir) {
        ProtobufIDLProxy.writeSourceCode(cd, sourceOutputDir);
    }
}
