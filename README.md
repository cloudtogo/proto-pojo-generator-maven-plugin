# proto-pojo-generator-maven-plugin

generate pojo with .proto file

## usage

```
<plugin>
    <groupId>com.cloudtogo.plugins</groupId>
    <artifactId>proto-pojo-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <protoModules>
            <protoModule>
                <proto>src/test/resources/protobuf/monitor.proto</proto>
                <pkg>com.cloudtogo.proto.pojo.monitor</pkg>
            </protoModule>
        </protoModules>
    </configuration>
</plugin>
```

- proto: `.proto` file or directory
- pkg: source code package
