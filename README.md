### Usage
Maven plugin to use in loadable plugins.
```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-antrun-plugin</artifactId>
  <version>1.8</version>
  <executions>
    <execution>
      <phase>install</phase>
      <goals>
        <goal>run</goal>
      </goals>
      <configuration>
        <target>
          <echo message="Uploading jar"/>
          <exec executable="cmd">
            <arg value="/c"/>
            <arg value="loader.bat"/>
            <arg value="${build.finalName}"/>
          </exec>
        </target>
      </configuration>
    </execution>
  </executions>
</plugin>
```

download the loader.bat file, change the TOKEN to your github token and put it in the project base folder
