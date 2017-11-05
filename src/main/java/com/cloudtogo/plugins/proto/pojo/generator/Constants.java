package com.cloudtogo.plugins.proto.pojo.generator;

import com.baidu.bjf.remoting.protobuf.FieldType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cheney on 2017/10/28.
 */
public class Constants {

    public static final String Indent = "    ";
    public static final String NewLine = System.getProperty("line.separator");

    public static final String Indent(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(Indent);
        }
        return builder.toString();
    }

    public static final Map<String, FieldType> typeMapping = new HashMap<String, FieldType>() {{
        put("double", FieldType.DOUBLE);
        put("float", FieldType.FLOAT);
        put("int64", FieldType.INT64);
        put("uint64", FieldType.UINT64);
        put("int32", FieldType.INT32);
        put("fixed64", FieldType.FIXED64);
        put("fixed32", FieldType.FIXED32);
        put("bool", FieldType.BOOL);
        put("string", FieldType.STRING);
        put("bytes", FieldType.BYTES);
        put("uint32", FieldType.UINT32);
        put("sfixed32", FieldType.SFIXED32);
        put("sfixed64", FieldType.SFIXED64);
        put("sint64", FieldType.SINT64);
        put("sint32", FieldType.SINT32);
    }};
}
