## Important Directories:

### applibs
This is the directory that is automatically being scanned for the jar files when the `SwingletRunner.loadJarsFromDirectory()` is called.
The swing application jar file and its runtime dependency jar files should be in this directory so that they are put in an isolated classloader at runtime.
The name of this directory is important and case-sensitive.
If you wish to change it, you can pass the path to such a directory by passing `-Dapplibs.dir` from commandline.
It is also possible to configure this in the `pom.xml`. There is a commented placeholder for this in the `<systemPropertyVariables>` section of the `pom.xml`.

### swing-bridge-repo
This is the local maven repository for necessary libraries which are not publicly available in any repositories.

## How to Run
The `pom.xml` is configured with necessary command-line arguments and configurations, so you can run it in development mode simply by running:
```
mvn clean spring-boot:run
```

## Production Build
Also, similar to any spring-boot Vaadin application, it is possible to build it using:
```
mvn clean package -Pproduction
```

and the result is a Spring-Boot fat jar that can be run with:
```
java \
  --patch-module java.desktop=./swinglet-repo/raw/vaadin-openjdk17-swing-modpatch-0.0.1-SNAPSHOT.jar \
  -Xbootclasspath/a:./swinglet-repo/com/vaadin/vaadin-openjdk17-graphics/0.0.1-SNAPSHOT-local/vaadin-openjdk17-graphics-0.0.1-SNAPSHOT-local.jar \
  --add-reads java.desktop=ALL-UNNAMED \
  --add-exports=java.desktop/sun.font=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.dnd.peer=ALL-UNNAMED \
  --add-exports=java.base/sun.nio.cs=ALL-UNNAMED \
  --add-exports=java.desktop/sun.java2d=ALL-UNNAMED \
  --add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED \
  --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED \
  --add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED \
  --add-exports=java.desktop/sun.print=ALL-UNNAMED \
  --add-exports=java.desktop/sun.swing=ALL-UNNAMED \
  --add-opens=java.desktop/java.awt.event=ALL-UNNAMED \
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED \
  -Djava.awt.headless=false \
  -Dapplibs.dir=./applibs \
  -jar target/app-1.0-SNAPSHOT.jar
```
