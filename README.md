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

```
<plugin>
    <groupId>com.cloudtogo.plugins</groupId>
    <artifactId>proto-pojo-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <protoModules>
            <protoModule>
                <proto>src/test/resources/protobuf/cluster_meta.proto</proto>
                <pkg>com.cloudtogo.business.orca.model.meta</pkg>
                <fields>
                    <field>
                        <message>Container</message>
                        <field>TerminalKey</field>
                        <serialize>com.cloudtogo.business.core.serializer.Base64JsonSerializer</serialize>
                        <deserialize>com.cloudtogo.business.core.deserializer.Base64JsonDeserializer</deserialize>
                    </field>
                </fields>
            </protoModule>
            <protoModule>
                <proto>src/test/resources/protobuf/deploy_svc.proto</proto>
                <pkg>com.cloudtogo.business.orca.model.deploy</pkg>
            </protoModule>
        </protoModules>
    </configuration>
</plugin>
```

target:

```
<plugin>
    <groupId>com.cloudtogo.plugins</groupId>
    <artifactId>proto-pojo-generator-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <protoModules>
            <protoModule>
                <proto>src/test/resources/protobuf/cluster_meta.proto</proto>
                <model>com.cloudtogo.business.orca.proto.meta.model</model>
                <builder>com.cloudtogo.business.orca.proto.meta.builder</builder>
                <fields>
                    <field>
                        <message>Container</message>
                        <field>TerminalKey</field>
                        <serialize>com.cloudtogo.business.core.serializer.Base64JsonSerializer</serialize>
                        <deserialize>com.cloudtogo.business.core.deserializer.Base64JsonDeserializer</deserialize>
                    </field>
                </fields>
            </protoModule>
            <protoModule>
                <proto>src/test/resources/protobuf/deploy_svc.proto</proto>
                <model>com.cloudtogo.business.orca.proto.deploy.model</model>
                <builder>com.cloudtogo.business.orca.proto.meta.builder</builder>
            </protoModule>
        </protoModules>
    </configuration>
</plugin>
```

- proto: `.proto` file or directory
- pkg: source code package
