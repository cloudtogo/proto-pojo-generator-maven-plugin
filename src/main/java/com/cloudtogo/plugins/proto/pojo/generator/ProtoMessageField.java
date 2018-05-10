package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.FieldType;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import com.cloudtogo.plugins.proto.pojo.generator.config.model.SpecialField;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * if field type is proto message, then the dataType.kind == DataType.Kind.NAMED and the fieldType is null.
 * Created by cheney on 2017/10/27.
 */
public class ProtoMessageField {

    private ProtoMessageClass protoMessageClass;
    private ProtoFile protoFile;
    private FieldElement fieldElement;
    private List<ProtoMessageMap> protoMessageMapListPerhaps;
    private SpecialField specialField;
    private Log log;

    private String javaType;
    private List<String> pojoDependencyList = new ArrayList<>();
    private StringBuilder schema = new StringBuilder();
    private StringBuilder getMethod = new StringBuilder();
    private StringBuilder setMethod = new StringBuilder();

    public ProtoMessageField(ProtoMessageClass protoMessageClass, ProtoFile protoFile, FieldElement fieldElement, List<ProtoMessageMap> protoMessageMapListPerhaps, SpecialField specialField, Log log) {
        this.protoMessageClass = protoMessageClass;
        this.protoFile = protoFile;
        this.fieldElement = fieldElement;
        this.protoMessageMapListPerhaps = null == protoMessageMapListPerhaps ? new ArrayList<>() : protoMessageMapListPerhaps;
        this.specialField = specialField;
        this.log = null == log ? new SystemStreamLog() : log;
        this.init();
    }

    private void init() {
        String name = this.fieldElement.name();
        String getSetPart = StringUtils.capitalize(name);

        if (fieldElement.label() == FieldElement.Label.REPEATED) {
            String typeName = fieldElement.type().toString();
            if (fieldElement.type().kind() == DataType.Kind.SCALAR) {
                this.javaType = String.format("%s<%s>", List.class.getName(), Constants.typeMapping.get(typeName).getJavaType());
            } else {
                typeName = this.getNamedType(typeName);
                this.javaType = String.format("%s<%s>", List.class.getName(), typeName);
            }
        } else if (fieldElement.type().kind() == DataType.Kind.MAP) {
            DataType.MapType mapType = (DataType.MapType) fieldElement.type();
            String keyType = mapType.keyType().toString();
            if (null != Constants.typeMapping.get(keyType)) {
                keyType = Constants.typeMapping.get(keyType).getJavaType();
            } else {
                keyType = this.getNamedType(keyType);
            }
            String valueType = mapType.valueType().toString();
            if (null != Constants.typeMapping.get(valueType)) {
                valueType = Constants.typeMapping.get(valueType).getJavaType();
            } else {
                valueType = this.getNamedType(valueType);
            }
            this.javaType = String.format("%s<%s, %s>", Map.class.getName(), keyType, valueType);
        } else if (fieldElement.type().kind() == DataType.Kind.SCALAR) {
            String typeName = fieldElement.type().toString();
            this.javaType = Constants.typeMapping.get(typeName).getJavaType();
        } else if (fieldElement.type().kind() == DataType.Kind.NAMED) {
            // field type is proto message
            String str = fieldElement.type().toString();
            this.javaType = this.getNamedType(str);
        }

        String valName = StringUtils.uncapitalize(name);
        if (null != this.specialField && StringUtils.isNotBlank(this.specialField.getAlias())) {
            valName = this.specialField.getAlias();
        }
        if (null != this.specialField) {
            if (StringUtils.isNotBlank(this.specialField.getSerialize())) {
                this.schema.append(Constants.Indent(1)).append(String.format("@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = %s.class)", this.specialField.getSerialize())).append(Constants.NewLine);
            }
            if (StringUtils.isNotBlank(this.specialField.getDeserialize())) {
                this.schema.append(Constants.Indent(1)).append(String.format("@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = %s.class)", this.specialField.getDeserialize())).append(Constants.NewLine);
            }
        }
        this.schema.append(Constants.Indent(1)).append(String.format("private %s %s;", this.javaType, valName)).append(Constants.NewLine);

        this.getMethod.append(Constants.Indent(1)).append(String.format("public %s get%s() {", this.javaType, getSetPart)).append(Constants.NewLine);
        this.getMethod.append(Constants.Indent(2)).append(String.format("return %s;", valName)).append(Constants.NewLine);
        this.getMethod.append(Constants.Indent(1)).append("}").append(Constants.NewLine).append(Constants.NewLine);

        this.setMethod.append(Constants.Indent(1)).append(String.format("public void set%s(%s %s) {", getSetPart, this.javaType, valName)).append(Constants.NewLine);
        this.setMethod.append(Constants.Indent(2)).append(String.format("this.%s = %s;", valName, valName)).append(Constants.NewLine);
        this.setMethod.append(Constants.Indent(1)).append("}").append(Constants.NewLine).append(Constants.NewLine);
    }

    private String getNamedType(String typeName) {
        if (StringUtils.isBlank(typeName)) {
            return typeName;
        }
        if (typeName.indexOf(".") > 0) {
            String pkg = typeName.substring(0, typeName.lastIndexOf(".")); // 字段类型上定义的pkg，可能只是最后一部分内容
            typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
            ProtoMessageMap protoMessageMap = this.getDependency(pkg, typeName);
            if (null != protoMessageMap) {
                String dependency = protoMessageMap.getPojoPkg() + "." + protoMessageMap.getPojoClassName();
                if (!this.pojoDependencyList.contains(dependency)) {
                    this.pojoDependencyList.add(dependency);
                }
            }
            return typeName;
        } else {
            return typeName;
        }
    }

    /**
     * @param pkg in proto file
     * @param messageName message name
     * @return
     */
    private ProtoMessageMap getDependency(String pkg, String messageName) {
        for (ProtoMessageMap protoMessageMap : this.protoMessageMapListPerhaps) {
            if(protoMessageMap.getPkg().endsWith(pkg) && messageName.equals(protoMessageMap.getName())){
                return protoMessageMap;
            }
        }
        return null;
    }

    /**
     * @return field name
     */
    public String name() {
        return this.fieldElement.name();
    }

    public String getJavaType() {
        return this.javaType;
    }

    public List<String> getPojoDependencyList() {
        return pojoDependencyList;
    }

    public StringBuilder getSchema() {
        return this.schema;
    }

    public StringBuilder createGetMethodCode() {
        return this.getMethod;
    }

    public StringBuilder createSetMethodCode() {
        return this.setMethod;
    }

    public StringBuilder createBuildCode(String val, String arg) {
        this.initBuilder(val, arg);
        return buildCode;
    }

    public StringBuilder createUnbuildCode(String val, String arg) {
        this.initBuilder(val, arg);
        return unbuildCode;
    }

    public List<String> getBuilderDependencyList() {
        this.initBuilder("val", "arg");
        return builderDependencyList;
    }

    private void initBuilder(String val, String arg) {
        this.buildCode = new StringBuilder();
        this.unbuildCode = new StringBuilder();
        this.initBuildAndUnbuildCode(val, arg);
    }

    private StringBuilder buildCode = new StringBuilder();
    private StringBuilder unbuildCode = new StringBuilder();
    private List<String> builderDependencyList = new ArrayList<>();

    private void initBuildAndUnbuildCode(String val, String arg) {
        String get = arg + ".get" + this.getFieldName() + "()";
        if (this.fieldElement.label() == FieldElement.Label.REPEATED) {
            if (fieldElement.type().kind() == DataType.Kind.SCALAR) {
                this.buildAndUnbuildListJavaType(val, arg, get);
            } else {
                this.buildAndUnbuildListNamedType(val, arg, get);
            }
        } else if (fieldElement.type().kind() == DataType.Kind.MAP) {
            this.buildAndUnbuildMap(val, arg, get);
        } else if (fieldElement.type().kind() == DataType.Kind.SCALAR) {
            this.buildAndUnbuildJavaType(val, get);
        } else if (fieldElement.type().kind() == DataType.Kind.NAMED) {
            this.buildAndUnbuildNamedType(val, arg, get);
        }
    }

    private void buildAndUnbuildListJavaType(String val, String arg, String get) {
                /*
        if (null != dockerImage.getNames() && dockerImage.getNames().size() > 0) {
            builder.addAllNames(dockerImage.getNames());
        }
                 */
        buildCode.append(Constants.Indent(2))
                .append(String.format("if (null != %s && %s.size() > 0) {", get, get))
                .append(Constants.NewLine);
        buildCode.append(Constants.Indent(3))
                .append(String.format("%s.addAll%s(%s);", val, this.getFieldName(), get))
                .append(Constants.NewLine);
        buildCode.append(Constants.Indent(2)).append("}").append(Constants.NewLine);

        // dockerImage.setNames(arg.getNamesList());
        String listGet = arg + ".get" + this.getFieldName() + "List()";
        unbuildCode.append(Constants.Indent(2)).append(String.format("%s.set%s(%s);", val, this.getFieldName(), listGet)).append(Constants.NewLine);
    }

    private void buildAndUnbuildListNamedType(String val, String arg, String get) {
                /*
        if (null != clusterMeta.getNode() && clusterMeta.getNode().size() > 0) {
            for(com.cloudtogo.business.orca.proto.meta.model.NodeInfo nodeInfo: clusterMeta.getNode()) {
                meta.ClusterMetaOuterClass.NodeInfo.Builder nodeInfoBuilder = NodeInfoBuilder.build(nodeInfo);
                if (null != nodeInfoBuilder) builder.addNode(nodeInfoBuilder);
            }
        }
                 */
        String itemTypeName = fieldElement.type().toString();
        DependencyType dependencyType = this.getProtoType(itemTypeName);
        buildCode.append(Constants.Indent(2))
                .append(String.format("if (null != %s && %s.size() > 0) {", get, get))
                .append(Constants.NewLine);
        buildCode.append(Constants.Indent(3))
                .append(String.format("for (%s %s: %s) {", dependencyType.getPojoType(), StringUtils.uncapitalize(dependencyType.getPojoType()), get))
                .append(Constants.NewLine);
        buildCode.append(Constants.Indent(4))
                .append(String.format("%s.Builder %sBuilder = %s.build(%s);",
                        dependencyType.getProtoType(), StringUtils.uncapitalize(dependencyType.getPojoType()), dependencyType.getBuilderType(), StringUtils.uncapitalize(dependencyType.getPojoType())))
                .append(Constants.NewLine);
        buildCode.append(Constants.Indent(4))
                .append(String.format("if (null != %sBuilder) %s.add%s(%sBuilder);",
                        StringUtils.uncapitalize(dependencyType.getPojoType()), val, this.getFieldName(), StringUtils.uncapitalize(dependencyType.getPojoType())))
                .append(Constants.NewLine);
        buildCode.append(Constants.Indent(3)).append("}").append(Constants.NewLine);
        buildCode.append(Constants.Indent(2)).append("}").append(Constants.NewLine);
        /*
        if(null != arg.getNodeList()){
            List<Node> list = new ArrayList<>();
            arg.getNodeList().forEach(node -> list.add(NodeBuilder.unbuild(node)));
            clusterStats.setNode(list);
        }
         */
        String listGet = arg + ".get" + this.getFieldName() + "List()";
        unbuildCode.append(Constants.Indent(2)).append(String.format("if(null != %s){", listGet)).append(Constants.NewLine);
        unbuildCode.append(Constants.Indent(3)).append(String.format("%s<%s> list = new %s<>();", List.class.getName(), dependencyType.getPojoType(), ArrayList.class.getName())).append(Constants.NewLine);
        unbuildCode.append(Constants.Indent(3)).append(String.format("%s.forEach(%s -> list.add(%s.unbuild(%s)));",
                listGet, StringUtils.uncapitalize(dependencyType.getPojoType()), dependencyType.getBuilderType(), StringUtils.uncapitalize(dependencyType.getPojoType()))).append(Constants.NewLine);
        unbuildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(list);", val, this.getFieldName())).append(Constants.NewLine);
        unbuildCode.append(Constants.Indent(2)).append("}").append(Constants.NewLine);
    }

    private void buildAndUnbuildMap(String val, String arg, String get) {
        DataType.MapType mapType = (DataType.MapType) fieldElement.type();
        // TODO check keyType is named
        String valueType = mapType.valueType().toString();
        buildCode.append(Constants.Indent(2))
                .append(String.format("if (null != %s && %s.size() > 0) {", get, get))
                .append(Constants.NewLine);
        if (null != Constants.typeMapping.get(valueType)) {
                /*
        if (null != cluster.getTag() && cluster.getTag().size() > 0) {
            builder.putAllTag(cluster.getTag());
        }
                 */
            buildCode.append(Constants.Indent(3))
                    .append(String.format("%s.putAll%s(%s);", val, this.getFieldName(), get))
                    .append(Constants.NewLine);
        } else {
            // node.getComponents().forEach((k, v) -> builder.putComponents(k, ComponentListBuilder.build(v).build()));
            valueType = this.getNamedType(valueType);
            buildCode.append(Constants.Indent(3))
                    .append(String.format("%s.forEach((k, v) -> %s.put%s(k, %sBuilder.build(v).build()));",
                            get, val, this.getFieldName(), valueType))
                    .append(Constants.NewLine);
        }
        buildCode.append(Constants.Indent(2)).append("}").append(Constants.NewLine);

        String mapGet = arg + ".get" + this.getFieldName() + "Map()";
        if (null != Constants.typeMapping.get(valueType)) {
            // component.setLabels(arg.getLabelsMap());
            unbuildCode.append(Constants.Indent(2)).append(String.format("%s.set%s(arg.get%sMap());", val, this.getFieldName(), this.getFieldName(), mapGet)).append(Constants.NewLine);
        } else {
        /*
        if (null != arg.getClusterMap()) {
            java.util.Map<String, com.cloudtogo.business.orca.proto.types.model.Cluster> map = new java.util.HashMap<>();
            arg.getClusterMap().forEach((k, v) -> map.put(k, ClusterBuilder.unbuild(v)));
            dCStats.setCluster(map);
        }
         */
            DependencyType valueDependencyType = this.getProtoType(mapType.valueType().toString());
            unbuildCode.append(Constants.Indent(2)).append(String.format("if (null != %s) {", mapGet)).append(Constants.NewLine);
            unbuildCode.append(Constants.Indent(3)).append(String.format("%s map = new %s<>();", this.javaType, HashMap.class.getName())).append(Constants.NewLine);
            unbuildCode.append(Constants.Indent(3)).append(String.format("%s.forEach((k, v) -> map.put(k, %s.unbuild(v)));", mapGet, valueDependencyType.getBuilderType())).append(Constants.NewLine);
            unbuildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(map);", val, this.getFieldName())).append(Constants.NewLine);
            unbuildCode.append(Constants.Indent(2)).append("}").append(Constants.NewLine);
        }
    }

    private void buildAndUnbuildJavaType(String val, String get){
        String typeName = fieldElement.type().toString();
        if (Constants.typeMapping.get(typeName) == FieldType.STRING) {
            buildCode.append(Constants.Indent(2)).append(String.format("if (null != %s && %s.trim().length() > 0) {", get, get)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(%s);", val, this.getFieldName(), get)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(2)).append(String.format("}")).append(Constants.NewLine);
        } else if (Constants.typeMapping.get(typeName) == FieldType.BYTES) {
            buildCode.append(Constants.Indent(2)).append(String.format("if (null != %s && %s.length > 0) {", get, get)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(com.google.protobuf.ByteString.copyFrom(%s));", val, this.getFieldName(), get)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(2)).append(String.format("}")).append(Constants.NewLine);
        } else {
            buildCode.append(Constants.Indent(2)).append(String.format("if (null != %s) {", get)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(%s);", val, this.getFieldName(), get)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(2)).append(String.format("}")).append(Constants.NewLine);
        }

        if (Constants.typeMapping.get(typeName) == FieldType.BYTES) {
            unbuildCode.append(Constants.Indent(2)).append(String.format("if (null != %s) {", get)).append(Constants.NewLine);
            unbuildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(%s.toByteArray());", val, this.getFieldName(), get)).append(Constants.NewLine);
            unbuildCode.append(Constants.Indent(2)).append(String.format("}")).append(Constants.NewLine);
        }else{
            unbuildCode.append(Constants.Indent(2)).append(String.format("%s.set%s(%s);", val, this.getFieldName(), get)).append(Constants.NewLine);
        }
    }

    private void buildAndUnbuildNamedType(String val, String arg, String get) {
        String typeName = fieldElement.type().toString();
        DependencyType dependencyType = this.getProtoType(typeName);
        if (this.isEnumType(typeName)) {
            // builder.setStatus(DeploySvc.EnumRPCStatus.forNumber(arg.getStatus().value()));
            buildCode.append(Constants.Indent(2))
                    .append(String.format("%s.set%s(%s.%s.forNumber(%s.value()));", val, this.getFieldName(), this.protoMessageClass.getProtocClassName(), typeName, get))
                    .append(Constants.NewLine);

            // nodeResponse.setStatus(com.cloudtogo.business.orca.proto.deploy.model.EnumRPCStatus.valueOf(arg.getStatusValue()));
            String enumGet = arg + ".get" + this.getFieldName() + "Value()";
            unbuildCode.append(Constants.Indent(2)).append(String.format("%s.set%s(%s.valueOf(%s));", val, this.getFieldName(), typeName, enumGet)).append(Constants.NewLine);
        } else {
        /*
        types.Type.NodeCapacity.Builder capacityBuilder = NodeCapacityBuilder.build(node.getCapacity());
        if (null != capacityBuilder) builder.setCapacity(capacityBuilder);
         */
            String tempVal = StringUtils.uncapitalize(this.getFieldName()) + "Builder";
            buildCode.append(Constants.Indent(2))
                    .append(String.format("%s.Builder %s = %sBuilder.build(%s);",
                            this.getProtoType(typeName).getProtoType(), tempVal, this.javaType, get))
                    .append(Constants.NewLine);
            buildCode.append(Constants.Indent(2)).append(String.format("if (null != %s) {", tempVal)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(3)).append(String.format("%s.set%s(%s);", val, this.getFieldName(), tempVal)).append(Constants.NewLine);
            buildCode.append(Constants.Indent(2)).append("}").append(Constants.NewLine);

            // dCSpec.setDefault(HostBuilder.unbuild(arg.getDefault()));
            unbuildCode.append(Constants.Indent(2)).append(String.format("%s.set%s(%s.unbuild(%s));", val, this.getFieldName(), dependencyType.getBuilderType(), get)).append(Constants.NewLine);
        }
    }

    private boolean isEnumType(String typeName) {
        if (StringUtils.isBlank(typeName) || typeName.indexOf(".") > 0) {
            return false;
        }
        for (TypeElement typeElement : this.protoFile.typeElements()) {
            if (typeElement instanceof EnumElement && typeElement.name().equals(typeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param typeName named type, eg: types.ClusterSpec Spec = 3; then the typeName is types.ClusterSpec
     * @return Type.ClusterSpec and the Type is the protoc class
     */
    private DependencyType getProtoType(String typeName) {
        DependencyType dependencyType = new DependencyType();
        if (StringUtils.isBlank(typeName)) {
            this.log.error("type name is null");
            return dependencyType;
        }
        // import pojo class
        if (typeName.indexOf(".") > 0) {
            String pkg = typeName.substring(0, typeName.lastIndexOf("."));
            typeName = typeName.substring(typeName.lastIndexOf(".") + 1);
            dependencyType.setPojoType(typeName);
            ProtoMessageMap protoMessageMap = this.getDependency(pkg, typeName);
            if (null != protoMessageMap) {
                String dependency;
                dependency = protoMessageMap.getPojoPkg() + "." + protoMessageMap.getPojoClassName();
                if (!this.builderDependencyList.contains(dependency)) {
                    this.builderDependencyList.add(dependency);
                }
                dependency = protoMessageMap.getBuilderPkg() + "." + protoMessageMap.getBuilderClassName();
                if (!this.builderDependencyList.contains(dependency)) {
                    this.builderDependencyList.add(dependency);
                }
                dependency = protoMessageMap.getProtocClassPkg() + "." + protoMessageMap.getProtocClassName();
                if (!this.builderDependencyList.contains(dependency)) {
                    this.builderDependencyList.add(dependency);
                }
                typeName = protoMessageMap.getProtocClassName() + "." + typeName;
                dependencyType.setPojoType(protoMessageMap.getPojoClassName());
                dependencyType.setProtoType(typeName);
                dependencyType.setBuilderType(protoMessageMap.getBuilderClassName());
            } else {
                this.log.error("[typeName = " + typeName + "] dependency mapping is null");
            }
        } else {
            String dependency = this.protoMessageClass.getPojoPkg() + "." + typeName;
            if (!this.builderDependencyList.contains(dependency)) {
                this.builderDependencyList.add(dependency);
            }
            dependencyType.setPojoType(typeName);
            dependencyType.setProtoType(this.protoMessageClass.getProtocClassName() + "." + typeName);
            dependencyType.setBuilderType(typeName + "Builder");
        }
        return dependencyType;
    }

    private String getFieldName() {
        return StringUtils.capitalize(this.name());
    }

    class DependencyType {
        // eg: Host
        private String pojoType;
        // eg: Type.Host
        private String protoType;
        // eg: HostBuilder
        private String builderType;

        public String getPojoType() {
            return pojoType;
        }

        public void setPojoType(String pojoType) {
            this.pojoType = pojoType;
        }

        public String getProtoType() {
            return protoType;
        }

        public void setProtoType(String protoType) {
            this.protoType = protoType;
        }

        public String getBuilderType() {
            return builderType;
        }

        public void setBuilderType(String builderType) {
            this.builderType = builderType;
        }
    }
}
