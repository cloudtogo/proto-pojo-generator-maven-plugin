# proto-pojo-generator-maven-plugin

Pojo and builder code generator.

It will generate pojo and builder code with `.proto` files. It dependencies on [`com.baidu:jprotobuf`](https://github.com/jhunters/jprotobuf).

## usage

```
<plugin>
    <groupId>com.cloudtogo.plugins</groupId>
    <artifactId>proto-pojo-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

This plugin will find `src/resources/proto-pojo.xml`, and also you can config your special file like below.

```
<configuration>
    <config>src/test/resources/proto-pojo-config.xml</config>
</configuration>
```

`proto-pojo.xml` example:

```
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <protos>
        <proto>
            <file>src/test/resources/protobuf/helloworld.proto</file>
            <pojo-pkg>com.cloudtogo.helloword.model</pojo-pkg>
        </proto>
    </protos>
</config>
```

`helloworld.proto` example:

```
syntax = "proto3";
package hellword;

service MonitorService {
    rpc Hello (Request) returns (Response);
}

message Request {
    string name = 1;
    bytes content = 2;
}

message Response {
    bytes reply = 1;
}
```

### builder

Usually we have to convert the pojo class to a proto builder, and it's very simple with this plugin.

```
<proto>
    <file>src/test/resources/protobuf/helloworld.proto</file>
    <pojo-pkg>com.cloudtogo.helloword.model</pojo-pkg>
    <builder-pkg>com.cloudtogo.helloword.builder</builder-pkg>
</proto>
```

Then `RequestBuilder` class and `ResponseBuilder` class will be generated.

### Serialize && Deserialize

example:

```
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <protos>
        <proto>
            <file>src/test/resources/protobuf/helloworld.proto</file>
            <pojo-pkg>com.cloudtogo.helloword.model</pojo-pkg>
            <builder-pkg>com.cloudtogo.helloword.builder</builder-pkg>
            <fields>
                <field>
                    <message>Request</message>
                    <field>content</field>
                    <serialize>com.cloudtogo.serializer.Base64JsonSerializer</serialize>
                    <deserialize>com.cloudtogo.deserializer.Base64JsonDeserializer</deserialize>
                </field>
                <field>
                    <message>Response</message>
                    <field>reply</field>
                    <serialize>com.cloudtogo.serializer.Base64JsonSerializer</serialize>
                    <deserialize>com.cloudtogo.deserializer.Base64JsonDeserializer</deserialize>
                </field>
            </fields>
        </proto>
    </protos>
</config>
```

And the pojo class will like this.

```
@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.cloudtogo.serializer.Base64JsonSerializer.class)
@com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = com.cloudtogo.deserializer.Base64JsonDeserializer.class)
private byte[] content;
```

So you will notice that `Serialize && Deserialize` only support `jackson`.

### alias

It is too bad if a field name is a key word in java. And you have to rename the field.

example:

```
<field>
    <message>Request</message>
    <field>name</field>
    <alias>nickname</alias>
</field>
```

And the pojo class will like this.

```
private String nickname;

public String getName() {
    return nickname;
}

public void setName(String nickname) {
    this.nickname = nickname;
}
```