/*
 * Copyright 2002-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.bjf.remoting.protobuf;

import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;
import com.baidu.bjf.remoting.protobuf.utils.CodePrinter;
import com.baidu.bjf.remoting.protobuf.utils.JDKCompilerHelper;
import com.baidu.bjf.remoting.protobuf.utils.StringUtils;
import com.baidu.jprotobuf.com.squareup.protoparser.*;
import com.baidu.jprotobuf.com.squareup.protoparser.FieldElement.Label;
import com.baidu.jprotobuf.com.squareup.protoparser.OptionElement.Kind;

import java.io.*;
import java.util.*;

/**
 * This class is for dynamic create protobuf utility class directly from .proto file
 *
 * @author xiemalin
 * @since 1.0.2
 */
public class ProtobufIDLProxy {

    private static final char PACKAGE_SPLIT_CHAR = '.';

    private static final String PACKAGE_SPLIT = PACKAGE_SPLIT_CHAR + "";
    /**
     *
     */
    private static final String UTF_8 = "utf-8";

    /**
     * java outer class name
     */
    private static final String JAVA_OUTER_CLASSNAME_OPTION = "java_outer_classname";

    /**
     * java package
     */
    private static final String JAVA_PACKAGE_OPTION = "java_package";

    /**
     * code line end wrap
     */
    public static final String CODE_END = ";\n";

    /**
     * default proto file name
     */
    public static final String DEFAULT_FILE_NAME = "jprotobuf_autogenerate";

    /**
     * type mapping of field type
     */
    public static final Map<String, FieldType> typeMapping;

    /**
     * type mapping of field type in string
     */
    private static final Map<String, String> fieldTypeMapping;

    static {

        typeMapping = new HashMap<String, FieldType>();

        typeMapping.put("double", FieldType.DOUBLE);
        typeMapping.put("float", FieldType.FLOAT);
        typeMapping.put("int64", FieldType.INT64);
        typeMapping.put("uint64", FieldType.UINT64);
        typeMapping.put("int32", FieldType.INT32);
        typeMapping.put("fixed64", FieldType.FIXED64);
        typeMapping.put("fixed32", FieldType.FIXED32);
        typeMapping.put("bool", FieldType.BOOL);
        typeMapping.put("string", FieldType.STRING);
        typeMapping.put("bytes", FieldType.BYTES);
        typeMapping.put("uint32", FieldType.UINT32);
        typeMapping.put("sfixed32", FieldType.SFIXED32);
        typeMapping.put("sfixed64", FieldType.SFIXED64);
        typeMapping.put("sint64", FieldType.SINT64);
        typeMapping.put("sint32", FieldType.SINT32);

        fieldTypeMapping = new HashMap<String, String>();

        fieldTypeMapping.put("double", "FieldType.DOUBLE");
        fieldTypeMapping.put("float", "FieldType.FLOAT");
        fieldTypeMapping.put("int64", "FieldType.INT64");
        fieldTypeMapping.put("uint64", "FieldType.UINT64");
        fieldTypeMapping.put("int32", "FieldType.INT32");
        fieldTypeMapping.put("fixed64", "FieldType.FIXED64");
        fieldTypeMapping.put("fixed32", "FieldType.FIXED32");
        fieldTypeMapping.put("bool", "FieldType.BOOL");
        fieldTypeMapping.put("string", "FieldType.STRING");
        fieldTypeMapping.put("bytes", "FieldType.BYTES");
        fieldTypeMapping.put("uint32", "FieldType.UINT32");
        fieldTypeMapping.put("sfixed32", "FieldType.SFIXED32");
        fieldTypeMapping.put("sfixed64", "FieldType.SFIXED64");
        fieldTypeMapping.put("sint64", "FieldType.SINT64");
        fieldTypeMapping.put("sint32", "FieldType.SINT32");
        fieldTypeMapping.put("enum", "FieldType.ENUM");
    }

    /**
     * auto proxied suffix class name
     */
    private static final String DEFAULT_SUFFIX_CLASSNAME = "JProtoBufProtoClass";

    public static IDLProxyObject createSingle(String data) {
        return createSingle(data, false);
    }

    public static IDLProxyObject createSingle(String data, boolean debug) {
        return createSingle(data, debug, null);
    }

    public static IDLProxyObject createSingle(String data, boolean debug, File path) {
        ProtoFile protoFile = ProtoParser.parse(DEFAULT_FILE_NAME, data);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        Map<String, IDLProxyObject> map;
        try {
            map = doCreate(protoFile, false, debug, path, false, null, cds);
            return map.entrySet().iterator().next().getValue();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static IDLProxyObject createSingle(InputStream is) throws IOException {
        return createSingle(is, false);
    }

    public static IDLProxyObject createSingle(InputStream is, boolean debug) throws IOException {
        return createSingle(is, debug, null);
    }

    public static IDLProxyObject createSingle(InputStream is, boolean debug, File path) throws IOException {
        ProtoFile protoFile = ProtoParser.parseUtf8(DEFAULT_FILE_NAME, is);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        Map<String, IDLProxyObject> map = doCreate(protoFile, false, debug, path, false, null, cds);
        return map.entrySet().iterator().next().getValue();
    }

    public static IDLProxyObject createSingle(Reader reader) throws IOException {
        return createSingle(reader, false);
    }

    public static IDLProxyObject createSingle(Reader reader, boolean debug) throws IOException {
        return createSingle(reader, debug, null);
    }

    public static IDLProxyObject createSingle(Reader reader, boolean debug, File path) throws IOException {
        ProtoFile protoFile = ProtoParser.parse(DEFAULT_FILE_NAME, reader);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        Map<String, IDLProxyObject> map = doCreate(protoFile, false, debug, path, false, null, cds);
        return map.entrySet().iterator().next().getValue();
    }

    public static Map<String, IDLProxyObject> create(String data) {
        return create(data, false);
    }

    public static Map<String, IDLProxyObject> create(String data, boolean debug) {
        return create(data, debug, null);
    }

    public static Map<String, IDLProxyObject> create(String data, boolean debug, File path) {
        ProtoFile protoFile = ProtoParser.parse(DEFAULT_FILE_NAME, data);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        try {
            return doCreate(protoFile, true, debug, path, false, null, cds);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Map<String, IDLProxyObject> create(InputStream is) throws IOException {
        return create(is, false);
    }

    public static Map<String, IDLProxyObject> create(InputStream is, boolean debug) throws IOException {
        return create(is, debug, null);
    }

    public static Map<String, IDLProxyObject> create(InputStream is, boolean debug, File path) throws IOException {
        ProtoFile protoFile = ProtoParser.parseUtf8(DEFAULT_FILE_NAME, is);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        return doCreate(protoFile, true, debug, path, false, null, cds);
    }

    public static Map<String, IDLProxyObject> create(Reader reader) throws IOException {
        return create(reader, false);
    }

    public static Map<String, IDLProxyObject> create(Reader reader, boolean debug) throws IOException {
        return create(reader, debug, null);
    }

    public static Map<String, IDLProxyObject> create(Reader reader, boolean debug, File path) throws IOException {
        ProtoFile protoFile = ProtoParser.parse(DEFAULT_FILE_NAME, reader);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        return doCreate(protoFile, true, debug, path, false, null, cds);
    }

    public static Map<String, IDLProxyObject> create(File file) throws IOException {
        return create(file, false);
    }

    public static Map<String, IDLProxyObject> create(File file, boolean debug) throws IOException {
        return create(file, debug, null);
    }

    public static Map<String, IDLProxyObject> create(File file, boolean debug, File path) throws IOException {
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        return create(file, debug, path, cds, new HashSet<String>());
    }

    public static Map<String, IDLProxyObject> create(File file, boolean debug, File path, List<CodeDependent> cds,
                                                     Set<String> compiledClass) throws IOException {

        return doCreatePro(file, true, debug, path, false, null, cds, compiledClass);
    }

    public static void generateSource(String data, File sourceOutputPath) {
        ProtoFile protoFile = ProtoParser.parse(DEFAULT_FILE_NAME, data);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        try {
            doCreate(protoFile, true, false, null, true, sourceOutputPath, cds);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static void generateSource(InputStream is, File sourceOutputPath) throws IOException {
        ProtoFile protoFile = ProtoParser.parseUtf8(DEFAULT_FILE_NAME, is);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        doCreate(protoFile, true, false, null, true, sourceOutputPath, cds);
    }

    public static void generateSource(Reader reader, File sourceOutputPath) throws IOException {
        ProtoFile protoFile = ProtoParser.parse(DEFAULT_FILE_NAME, reader);
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        doCreate(protoFile, true, false, null, true, sourceOutputPath, cds);
    }

    public static void generateSource(File file, File sourceOutputPath) throws IOException {
        List<CodeDependent> cds = new ArrayList<CodeDependent>();
        generateSource(file, sourceOutputPath, cds, new HashSet<String>());
    }

    public static void generateSource(File file, File sourceOutputPath, List<CodeDependent> cds,
                                      Set<String> compiledClass) throws IOException {

        doCreatePro(file, true, false, null, true, sourceOutputPath, cds, compiledClass);
    }

    private static Map<String, IDLProxyObject> doCreatePro(List<ProtoFile> protoFiles, boolean multi, boolean debug,
                                                           File path, boolean generateSouceOnly, File sourceOutputDir, List<CodeDependent> cds,
                                                           Set<String> compiledClass) throws IOException {

        int count = 0;

        Map<String, EnumElement> enumTypes = new HashMap<String, EnumElement>();
        // package mapping
        Map<String, String> packageMapping = new HashMap<String, String>();

        Set<String> packages = new HashSet<String>();
        for (ProtoFile protoFile : protoFiles) {

            List<TypeElement> types = protoFile.typeElements();
            if (types == null || types.isEmpty()) {
                continue;
            }

            String packageName = protoFile.packageName();
            // to check if option has "java_package"
            List<OptionElement> options = protoFile.options();
            if (options != null) {
                for (OptionElement option : options) {
                    if (option.name().equals(JAVA_PACKAGE_OPTION)) {
                        packageName = option.value().toString();
                    }
                }
            }
            packages.add(packageName);

            for (TypeElement type : types) {
                packageMapping.put(type.name(), packageName);
                packageMapping.put(type.qualifiedName(), packageName);

                if (type instanceof MessageElement) {
                    count++;
                } else {
                    enumTypes.put(type.name(), (EnumElement) type);
                    enumTypes.put(type.qualifiedName(), (EnumElement) type);
                }
            }
        }

        if (!multi && count != 1) {
            throw new RuntimeException("Only one message defined allowed in '.proto' IDL");
        }

        // create enum type classes
        List<Class<?>> clsList =
                createEnumClasses(enumTypes, packageMapping, generateSouceOnly, sourceOutputDir, compiledClass);

        for (ProtoFile protoFile : protoFiles) {
            // create message type classes
            List<Class<?>> messageClasses = createMessageClass(protoFile, multi, debug, generateSouceOnly,
                    sourceOutputDir, cds, compiledClass, new HashSet<String>(enumTypes.keySet()), packages);
            clsList.addAll(messageClasses);

        }

        Map<String, IDLProxyObject> ret = new HashMap<String, IDLProxyObject>();
        for (Class cls : clsList) {
            Object newInstance;
            try {
                if (Enum.class.isAssignableFrom(cls)) {
                    continue;
                }
                newInstance = cls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            Codec codec = ProtobufProxy.create(cls, debug, path);
            IDLProxyObject idlProxyObject = new IDLProxyObject(codec, newInstance, cls);

            String name = cls.getSimpleName();
            if (name.endsWith(DEFAULT_SUFFIX_CLASSNAME)) {
                name = name.substring(0, name.length() - DEFAULT_SUFFIX_CLASSNAME.length());
            }
            ret.put(name, idlProxyObject);
        }

        return ret;
    }

    private static Map<String, IDLProxyObject> doCreatePro(File file, boolean multi, boolean debug, File path,
                                                           boolean generateSouceOnly, File sourceOutputDir, List<CodeDependent> cds, Set<String> compiledClass)
            throws IOException {

        checkDirectory(generateSouceOnly, sourceOutputDir);

        // to find all PROTO file if using import command
        List<ProtoFile> protoFiles = findRelateProtoFiles(file, new HashSet<String>());
        Collections.reverse(protoFiles);
        return doCreatePro(protoFiles, multi, debug, path, generateSouceOnly, sourceOutputDir, cds, compiledClass);

    }

    private static List<Class<?>> createMessageClass(ProtoFile protoFile, boolean multi, boolean debug,
                                                     boolean generateSouceOnly, File sourceOutputDir, List<CodeDependent> cds, Set<String> compiledClass,
                                                     Set<String> enumNames, Set<String> packages) {

        List<TypeElement> types = protoFile.typeElements();
        if (types == null || types.isEmpty()) {
            throw new RuntimeException("No message defined in '.proto' IDL");
        }

        int count = 0;
        Iterator<TypeElement> iter = types.iterator();
        while (iter.hasNext()) {
            TypeElement next = iter.next();
            if (next instanceof EnumElement) {
                continue;
            }
            count++;
        }

        if (!multi && count != 1) {
            throw new RuntimeException("Only one message defined allowed in '.proto' IDL");
        }

        List<Class<?>> ret = new ArrayList<Class<?>>(types.size());

        List<MessageElement> messageTypes = new ArrayList<MessageElement>();
        for (TypeElement type : types) {
            Class checkClass = checkClass(protoFile, type);
            if (checkClass != null) {
                ret.add(checkClass);
                continue;
            }

            CodeDependent cd;
            if (type instanceof MessageElement) {
                messageTypes.add((MessageElement) type);
                continue;
            }
        }

        for (MessageElement mt : messageTypes) {
            CodeDependent cd;
            cd = createCodeByType(protoFile, (MessageElement) mt, enumNames, true, new ArrayList<TypeElement>(), cds,
                    packages);

            if (cd.isDepndency()) {
                cds.add(cd);
            } else {
                cds.add(0, cd);
            }
        }
        CodeDependent codeDependent;
        // copy cds
        List<CodeDependent> copiedCds = new ArrayList<ProtobufIDLProxy.CodeDependent>(cds);
        while ((codeDependent = hasDependency(copiedCds, compiledClass)) != null) {
            if (debug) {
                CodePrinter.printCode(codeDependent.code, "generate jprotobuf code");
            }
            if (!generateSouceOnly) {
                Class<?> newClass = JDKCompilerHelper.getJdkCompiler().compile(codeDependent.getClassName(),
                        codeDependent.code, ProtobufIDLProxy.class.getClassLoader(), null, -1);
                ret.add(newClass);
            } else {
                // need to output source code to target path
                writeSourceCode(codeDependent, sourceOutputDir);
            }
        }

        return ret;
    }

    private static List<Class<?>> createEnumClasses(Map<String, EnumElement> enumTypes,
                                                    Map<String, String> packageMapping, boolean generateSouceOnly, File sourceOutputDir,
                                                    Set<String> compiledClass) {

        List<Class<?>> ret = new ArrayList<Class<?>>();
        Set<String> enumNames = new HashSet<String>();
        Collection<EnumElement> enums = enumTypes.values();
        for (EnumElement enumType : enums) {
            String name = enumType.name();
            if (enumNames.contains(name)) {
                continue;
            }
            enumNames.add(name);
            String packageName = packageMapping.get(name);
            Class cls = checkClass(packageName, enumType);
            if (cls != null) {
                ret.add(cls);
                continue;
            }
            CodeDependent codeDependent = createCodeByType(enumType, true, packageName);
            compiledClass.add(codeDependent.name);
            compiledClass.add(packageName + PACKAGE_SPLIT_CHAR + codeDependent.name);
            if (!generateSouceOnly) {
                Class<?> newClass = JDKCompilerHelper.getJdkCompiler().compile(codeDependent.getClassName(),
                        codeDependent.code, ProtobufIDLProxy.class.getClassLoader(), null, -1);
                ret.add(newClass);
            } else {
                // need to output source code to target path
                writeSourceCode(codeDependent, sourceOutputDir);
            }
        }

        return ret;
    }

    /**
     * TODO
     *
     * @param generateSouceOnly
     * @param sourceOutputDir
     */
    protected static void checkDirectory(boolean generateSouceOnly, File sourceOutputDir) {
        if (generateSouceOnly) {
            if (sourceOutputDir == null) {
                throw new RuntimeException("param 'sourceOutputDir' is null.");
            }

            if (!sourceOutputDir.isDirectory()) {
                throw new RuntimeException("param 'sourceOutputDir' should be a exist file directory.");
            }
        }
    }

    private static List<ProtoFile> findRelateProtoFiles(File file, Set<String> dependencyNames) throws IOException {
        LinkedList<ProtoFile> protoFiles = new LinkedList<ProtoFile>();
        ProtoFile protoFile = ProtoParser.parseUtf8(file);
        protoFiles.addFirst(protoFile);

        String parent = file.getParent();
        // parse dependency, to find all PROTO file if using import command
        List<String> dependencies = protoFile.dependencies();
        if (dependencies != null && !dependencies.isEmpty()) {
            for (String fn : dependencies) {
                if (dependencyNames.contains(fn)) {
                    continue;
                }
                File dependencyFile = new File(parent, fn);
                protoFiles.addAll(findRelateProtoFiles(dependencyFile, dependencyNames));
            }
        }
        return protoFiles;
    }

    private static Map<String, IDLProxyObject> doCreate(ProtoFile protoFile, boolean multi, boolean debug, File path,
                                                        boolean generateSouceOnly, File sourceOutputDir, List<CodeDependent> cds) throws IOException {
        return doCreatePro(Arrays.asList(protoFile), multi, debug, path, generateSouceOnly, sourceOutputDir, cds,
                new HashSet<String>());
    }

    private static Set<String> getPackages(List<CodeDependent> cds) {
        Set<String> ret = new HashSet<String>();
        if (cds == null) {
            return ret;
        }
        for (CodeDependent cd : cds) {
            ret.add(cd.pkg);
        }

        return ret;
    }

    /**
     * @param cd
     * @param sourceOutputDir
     */
    public static void writeSourceCode(CodeDependent cd, File sourceOutputDir) {
        if (cd.pkg == null) {
            cd.pkg = "";
        }

        // mkdirs
        String dir = sourceOutputDir + File.separator + cd.pkg.replace('.', File.separatorChar);
        File f = new File(dir);
        f.mkdirs();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(f, cd.name + ".java"));
            fos.write(cd.code.getBytes(UTF_8));
            fos.flush();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }

    }

    private static CodeDependent hasDependency(List<CodeDependent> cds, Set<String> compiledClass) {
        if (cds.isEmpty()) {
            return null;
        }

        Iterator<CodeDependent> iterator = cds.iterator();
        while (iterator.hasNext()) {
            CodeDependent next = iterator.next();
            compiledClass.addAll(next.subClasses);
            if (!next.isDepndency()) {
                compiledClass.add(next.name);
                compiledClass.add(next.pkg + PACKAGE_SPLIT_CHAR + next.name);
                iterator.remove();
                return next;
            } else {
                Set<String> dependencies = next.dependencies;
                if (compiledClass.containsAll(dependencies)) {
                    compiledClass.add(next.name);
                    compiledClass.add(next.pkg + PACKAGE_SPLIT_CHAR + next.name);
                    iterator.remove();
                    return next;
                }
            }
        }

        // if cds is not empty guess there is some message dependency is not
        // available. so error idl protobuf defined?
        Set<String> guessLoadedClass = new HashSet<String>(compiledClass);
        if (!cds.isEmpty()) {
            iterator = cds.iterator();
            while (iterator.hasNext()) {
                CodeDependent codeDependent = iterator.next();
                guessLoadedClass.add(codeDependent.name);
            }

            // to check while message's dependency is missed
            iterator = cds.iterator();
            while (iterator.hasNext()) {
                CodeDependent next = iterator.next();
                if (!next.isDepndency()) {
                    continue;
                }

                if (guessLoadedClass.containsAll(next.dependencies)) {
                    continue;
                }

                for (String dependClass : next.dependencies) {
                    if (!guessLoadedClass.contains(dependClass)) {
                        throw new RuntimeException("Message '"
                                + StringUtils.removeEnd(next.name, DEFAULT_SUFFIX_CLASSNAME) + "' depend on message '"
                                + dependClass.replace(DEFAULT_SUFFIX_CLASSNAME, "") + "' is missed");
                    }
                }
            }

        }

        return null;
    }

    public static CodeDependent createCodeByType(EnumElement type, boolean topLevelClass, String packageName) {

        CodeDependent cd = new CodeDependent();

        String simpleName = type.name();


        // To generate class
        StringBuilder code = new StringBuilder();
        if (topLevelClass) {
            // define package
            code.append("package ").append(packageName).append(CODE_END);
            code.append(CodeDependent.NewLine);
        }

        // define class
        if (topLevelClass) {
            code.append("public enum ");
        } else {
            code.append("public static enum ");
        }
        code.append(simpleName).append(" {");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.NewLine);


        Iterator<EnumConstantElement> iter = type.constants().iterator();
        while (iter.hasNext()) {
            EnumConstantElement value = iter.next();
            String name = value.name();
            int tag = value.tag();

            code.append(CodeDependent.Indent);
            code.append(name).append("(").append(tag).append(")");
            if (iter.hasNext()) {
                code.append(",");
                code.append(CodeDependent.NewLine);
            } else {
                code.append(";");
                code.append(CodeDependent.NewLine);
                code.append(CodeDependent.NewLine);
            }
        }

        code.append(CodeDependent.Indent).append("private final int value;");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.NewLine);

        code.append(CodeDependent.Indent).append(simpleName).append("(int value) {");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.Indent).append(CodeDependent.Indent).append("this.value = value;");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.Indent).append("}");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.NewLine);

        code.append(CodeDependent.Indent).append("public int value() {");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.Indent).append(CodeDependent.Indent).append("return value;");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.Indent).append("}");
        code.append(CodeDependent.NewLine);
        code.append(CodeDependent.NewLine);
        code.append("}");
        code.append(CodeDependent.NewLine);

        cd.name = simpleName;
        cd.pkg = packageName;
        cd.code = code.toString();

        return cd;
    }

    public static CodeDependent createCodeByType(ProtoFile protoFile, MessageElement type, Set<String> enumNames,
                                                  boolean topLevelClass, List<TypeElement> parentNestedTypes, List<CodeDependent> cds, Set<String> packages) {

        CodeDependent cd = new CodeDependent();

        String packageName = protoFile.packageName();
        String defaultClsName = type.name();
        // to check if has "java_package" option and "java_outer_classname"
        List<OptionElement> options = protoFile.options();
        if (options != null) {
            for (OptionElement option : options) {
                if (option.name().equals(JAVA_PACKAGE_OPTION)) {
                    packageName = option.value().toString();
                }
            }
        }

        String simpleName = defaultClsName + DEFAULT_SUFFIX_CLASSNAME;

        // To generate class
        StringBuilder code = new StringBuilder();
        if (topLevelClass) {
            // define package
            code.append("package ").append(packageName).append(CODE_END);
            code.append("\n");
            // add import;
            code.append("import com.baidu.bjf.remoting.protobuf.FieldType;\n");
            code.append("import com.baidu.bjf.remoting.protobuf.EnumReadable;\n");
            code.append("import com.baidu.bjf.remoting.protobuf.annotation.Protobuf;\n");
        }

        // define class
        String clsName;
        if (topLevelClass) {
            clsName = "public class ";
        } else {
            clsName = "public static class ";
        }
        code.append(clsName).append(simpleName).append(" {\n");

        List<FieldElement> fields = type.fields();

        // get nested types
        List<TypeElement> nestedTypes = fetchAllNestedTypes(type);
        List<TypeElement> checkNestedTypes = new ArrayList<TypeElement>(nestedTypes);

        // to check if has nested classes and check has Enum type
        for (TypeElement t : nestedTypes) {
            if (t instanceof EnumElement) {
                enumNames.add(t.name());
            }
        }

        checkNestedTypes.addAll(parentNestedTypes);

        for (FieldElement field : fields) {
            // define annotation
            generateProtobufDefinedForField(code, field, enumNames);

            DataType dataType = field.type();
            String typeName;
            if (dataType.kind() == DataType.Kind.MAP) {
                typeName = getTypeName(((DataType.MapType) dataType).keyType());
            } else {
                typeName = getTypeName(field);
            }

            FieldType fType = typeMapping.get(typeName);
            String javaType;
            if (fType == null) {
                javaType = getTypeName(field) + DEFAULT_SUFFIX_CLASSNAME;
                if (!isNestedTypeDependency(field.type(), checkNestedTypes)) {
                    cd.addDependency(javaType);
                }
            } else {
                javaType = fType.getJavaType();
            }

            // fix java type for map type
            if (dataType.kind() == DataType.Kind.MAP) {
                DataType.MapType mapType = (DataType.MapType) dataType;
                String keyType = mapType.keyType().toString();
                FieldType subType = typeMapping.get(keyType);
                String keyJavaType;
                if (subType == null) {
                    keyJavaType = keyType;
                } else {
                    keyJavaType = subType.getJavaType();
                }

                String valueType = mapType.valueType().toString();
                subType = typeMapping.get(valueType);
                String valueJavaType;
                if (subType == null) {
                    valueJavaType = valueType;
                } else {
                    valueJavaType = subType.getJavaType();
                }

                javaType = Map.class.getName() + "<" + keyJavaType + ", " + valueJavaType + ">";
            }

            // check if repeated type
            if (Label.REPEATED == field.label()) {
                javaType = List.class.getName() + "<" + javaType + ">";
            }

            // define field
            code.append("public ").append(javaType);
            code.append(" ").append(field.name());

            // check if has default
            OptionElement defaultOption = OptionElement.findByName(field.options(), "default");
            if (defaultOption != null) {
                code.append("=");
                Object defaultValue = defaultOption.value();
                // if is enum type
                if (defaultOption.kind() == Kind.ENUM) {
                    code.append(javaType).append(".").append(defaultValue);
                } else if (defaultOption.kind() == Kind.STRING) {
                    code.append("\"").append(defaultValue).append("\"");
                } else {
                    code.append(String.valueOf(defaultValue));
                }
            }

            code.append(CODE_END);
        }

        // to check if has nested classes
        if (nestedTypes != null && topLevelClass) {
            for (TypeElement t : nestedTypes) {
                CodeDependent nestedCd;
                if (t instanceof EnumElement) {
                    nestedCd = createCodeByType((EnumElement) t, false, packageName);
                    enumNames.add(t.name());
                } else {
                    nestedCd = createCodeByType(protoFile, (MessageElement) t, enumNames, false, checkNestedTypes, cds,
                            getPackages(cds));
                }

                code.append(nestedCd.code);
                // merge dependency
                cd.dependencies.addAll(nestedCd.dependencies);
            }
        }

        code.append("}\n");

        cd.name = simpleName;
        cd.pkg = packageName;
        cd.code = code.toString();

        // finally dependency should remove self
        cd.dependencies.remove(cd.name);
        return cd;
    }

    private static String getTypeName(FieldElement field) {
        DataType type = field.type();
        return type.toString();
    }

    private static String getTypeName(DataType type) {
        return type.toString();
    }

    /**
     * @param type
     * @return
     */
    private static List<TypeElement> fetchAllNestedTypes(MessageElement type) {
        List<TypeElement> ret = new ArrayList<TypeElement>();

        List<TypeElement> nestedTypes = type.nestedElements();
        ret.addAll(nestedTypes);
        for (TypeElement t : nestedTypes) {
            if (t instanceof MessageElement) {
                List<TypeElement> subNestedTypes = fetchAllNestedTypes((MessageElement) t);
                ret.addAll(subNestedTypes);
            }
        }
        return ret;
    }

    /**
     * @param type
     * @param nestedTypes
     * @return
     */
    private static boolean isNestedTypeDependency(DataType type, List<TypeElement> nestedTypes) {
        if (nestedTypes == null) {
            return false;
        }

        for (TypeElement t : nestedTypes) {
            if (type.kind().name().equals(t.name())) {
                return true;
            }
        }

        return false;
    }

    /**
     * to generate @Protobuf defined code for target field.
     *
     * @param code
     * @param field
     */
    private static void generateProtobufDefinedForField(StringBuilder code, FieldElement field, Set<String> enumNames) {
        code.append("@").append(Protobuf.class.getSimpleName()).append("(");

        String fieldType = fieldTypeMapping.get(getTypeName(field));
        if (fieldType == null) {
            if (enumNames.contains(getTypeName(field))) {
                fieldType = "FieldType.ENUM";
            } else {
                if (field.type().kind() == DataType.Kind.MAP) {
                    fieldType = "FieldType.MAP";
                } else {
                    fieldType = "FieldType.OBJECT";
                }

            }
        }

        code.append("fieldType=").append(fieldType);
        code.append(", order=").append(field.tag());
        if (FieldElement.Label.OPTIONAL == field.label()) {
            code.append(", required=false");
        } else if (Label.REQUIRED == field.label()) {
            code.append(", required=true");
        }
        code.append(")\n");

    }

    private static Class checkClass(String packageName, TypeElement type) {
        String simpleName = getProxyClassName(type.name());
        String className = packageName + PACKAGE_SPLIT_CHAR + simpleName;

        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            // if class not found so should generate a new java source class.
            c = null;
        }

        return c;
    }

    private static Class checkClass(ProtoFile protoFile, TypeElement type) {
        String packageName = protoFile.packageName();
        String defaultClsName = type.name();
        // to check if has "java_package" option and "java_outer_classname"
        List<OptionElement> options = protoFile.options();
        if (options != null) {
            for (OptionElement option : options) {
                if (option.name().equals(JAVA_PACKAGE_OPTION)) {
                    packageName = option.value().toString();
                } else if (option.name().equals(JAVA_OUTER_CLASSNAME_OPTION)) {
                    defaultClsName = option.value().toString();
                }
            }
        }

        String simpleName = getProxyClassName(defaultClsName);
        String className = packageName + PACKAGE_SPLIT_CHAR + simpleName;

        Class<?> c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e1) {
            // if class not found so should generate a new java source class.
            c = null;
        }

        return c;
    }

    private static String getProxyClassName(String name) {
        Set<String> emptyPkgs = Collections.emptySet();
        return getProxyClassName(name, emptyPkgs);
    }

    private static String getProxyClassName(String name, Set<String> pkgs) {

        String ret = "";
        if (name.indexOf(PACKAGE_SPLIT_CHAR) != -1) {
            String[] split = name.split("\\.");
            boolean classFound = false;
            for (String string : split) {
                if (pkgs.contains(string) && !classFound) {
                    ret += string + PACKAGE_SPLIT_CHAR;
                } else {
                    classFound = true;
                    ret += getProxyClassName(string) + PACKAGE_SPLIT_CHAR;
                }
            }
            ret = StringUtils.removeEnd(ret, PACKAGE_SPLIT);
        } else {
            ret = name + DEFAULT_SUFFIX_CLASSNAME;
        }
        return ret;
    }

    /**
     * google Protobuf IDL message dependency result
     *
     *
     * @author xiemalin
     * @since 1.0
     */
    public static class CodeDependent {

        public static final String Indent = "    ";
        public static final String NewLine = "\n";

        private String name;
        private String pkg;
        private Set<String> dependencies = new HashSet<String>();
        private String code;

        private Set<String> subClasses = new HashSet<String>();

        private boolean isDepndency() {
            return !dependencies.isEmpty();
        }

        private void addSubClass(String name) {
            subClasses.add(name);
        }

        private void addDependency(String name) {
            dependencies.add(name);
        }

        public String getClassName() {
            if (StringUtils.isEmpty(pkg)) {
                return name;
            }
            return pkg + PACKAGE_SPLIT_CHAR + name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPkg() {
            return pkg;
        }

        public void setPkg(String pkg) {
            this.pkg = pkg;
        }

        public Set<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(Set<String> dependencies) {
            this.dependencies = dependencies;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public Set<String> getSubClasses() {
            return subClasses;
        }

        public void setSubClasses(Set<String> subClasses) {
            this.subClasses = subClasses;
        }
    }
}
