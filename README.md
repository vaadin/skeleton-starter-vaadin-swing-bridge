# Overview
This minimal skeleton provides an example of using **Vaadin Swing Bridge** technology that runs existing Swing applications within a **Vaadin** to make them accessible for the end users through the web in no time.

This project uses maven for managing the dependencies and configurations.
Similar to any other Vaadin projects, you can run it from commandline `mvn clean spring-boot:run`.
For further instructions of building for productions, please refer to the [Production Build](#production-build) section.

For making the initial setup straightforward, Vaadin Swing Bridge runtime assumes some configurations are in place. 
These configurations are mostly about where the Swing application's jar file(s) and dependencies are located. 
Please refer to the [Important Directories](#important-directories) to make sure you have a smooth experience. :+1:

## Important Directories:

### applibs
This is the directory that is automatically being scanned for the jar files of the Swing App. 
Swing application packaging models can differ from exporting one fat jar, to having a light main jar plus its dependencies right next to it, or loaded at runtime in some way.
In any case, all the jar files should be in this directory so that they are available to the Swing Bridge runtime.

> **_NOTE:_**  The `applibs` name of this directory is important and case-sensitive.
If you wish to change it, you can pass the path to such a directory that contains all the jar files by passing `-Dapplibs.dir` from commandline.
It is also possible to configure this in the `pom.xml`. There is a commented placeholder for this in the `<systemPropertyVariables>` section of the `pom.xml`.

## Prerequisites
- **JDK 21 or later.** SwingBridge patches `java.desktop` at runtime and needs a Java 21 toolchain.
- A desktop-capable environment. SwingBridge renders Swing components on the server, so it runs with `java.awt.headless=false` and needs a working AWT toolkit (on a headless Linux server, an X server such as Xvfb).

Maven itself does not need to be installed: the project ships the Maven wrapper, so `./mvnw` (or `mvnw.cmd` on Windows) works out of the box. The commands below use `mvn`; substitute `./mvnw` if you prefer the wrapper.

## How to Run
The `pom.xml` is configured with necessary command-line arguments and configurations, so you can run it in development mode simply by running:
```
mvn clean spring-boot:run
```

Then open http://localhost:8888. To attach a debugger, connect your IDE to `localhost:5005`.

## Configuring SwingBridge
SwingBridge reads its optional settings from JVM system properties. The `<systemPropertyVariables>` section of the `pom.xml` lists the most useful ones as commented-out entries with a one-line explanation each — screen size, HiDPI rendering, back-buffer format, cold keyboard focus, per-session log prefixes, and the launch-failure error view. Uncomment what you need.

The full reference is in the [SwingBridge documentation](https://vaadin.com/docs/latest/tools/modernization-toolkit/swing-bridge/configuration).

### Telling users' log lines apart
Every browser session runs its own copy of the Swing application inside the same JVM, so their log output is interleaved by default. `src/main/resources/log4j2-spring.xml.example` is a ready-made Log4j2 configuration that tags each line with the session's run ID (and optionally the user's name). Its header explains the two steps needed to activate it.

## Production Build
Also, similar to any spring-boot Vaadin application, it is possible to build it using:
```
mvn clean package
```

and the result is a Spring-Boot fat jar that can be run with the commands below.

Two placeholders appear in the commands below.

`<SWING_BRIDGE_VERSION>` is the `swing-bridge.version` property from `pom.xml` — currently **1.3.0**. Read it from the pom rather than copying a version from here, so the two cannot drift:
```
mvn help:evaluate -Dexpression=swing-bridge.version -q -DforceStdout
```

`<MAVEN_REPO>` refers to your local Maven repository path:
- **Linux/macOS**: `$HOME/.m2/repository` (do **not** use `~` as it won't expand inside JVM flags like `-Xbootclasspath/a:`)
- **Windows**: `C:\Users\<your-username>\.m2\repository`

If you have a custom repository location, you can find the exact path by running:
```
mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout
```

> **_NOTE:_** These flags are the same set the `pom.xml` passes to `spring-boot:run`, minus the debug agent. Keep the two in sync if you change one.

**Linux/macOS:**
```bash
java \
  --patch-module java.desktop=<MAVEN_REPO>/com/vaadin/swing-bridge-patch/<SWING_BRIDGE_VERSION>/swing-bridge-patch-<SWING_BRIDGE_VERSION>.jar \
  -Xbootclasspath/a:<MAVEN_REPO>/com/vaadin/swing-bridge-graphics/<SWING_BRIDGE_VERSION>/swing-bridge-graphics-<SWING_BRIDGE_VERSION>.jar \
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
  --add-opens=java.desktop/java.awt.dnd=ALL-UNNAMED \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  -Djava.awt.headless=false \
  -Dapplibs.dir=./applibs \
  -jar target/skeleton-starter-vaadin-swing-bridge-1.0-SNAPSHOT.jar
```

**Windows (Command Prompt):**
```cmd
java ^
  --patch-module java.desktop=<MAVEN_REPO>\com\vaadin\swing-bridge-patch\<SWING_BRIDGE_VERSION>\swing-bridge-patch-<SWING_BRIDGE_VERSION>.jar ^
  -Xbootclasspath/a:<MAVEN_REPO>\com\vaadin\swing-bridge-graphics\<SWING_BRIDGE_VERSION>\swing-bridge-graphics-<SWING_BRIDGE_VERSION>.jar ^
  --add-reads java.desktop=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.font=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.dnd=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.dnd.peer=ALL-UNNAMED ^
  --add-exports=java.base/sun.nio.cs=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.java2d=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.java2d.pipe=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.datatransfer=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.awt.image=ALL-UNNAMED ^
  --add-exports=java.desktop/java.awt.peer=ALL-UNNAMED ^
  --add-exports=java.desktop/java.awt.dnd=ALL-UNNAMED ^
  --add-exports=java.desktop/java.awt.dnd.peer=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.print=ALL-UNNAMED ^
  --add-exports=java.desktop/sun.swing=ALL-UNNAMED ^
  --add-opens=java.desktop/java.awt.event=ALL-UNNAMED ^
  --add-opens=java.desktop/sun.awt=ALL-UNNAMED ^
  --add-opens=java.desktop/java.awt.dnd=ALL-UNNAMED ^
  --add-opens=java.base/java.lang=ALL-UNNAMED ^
  -Djava.awt.headless=false ^
  -Dapplibs.dir=.\applibs ^
  -jar target\skeleton-starter-vaadin-swing-bridge-1.0-SNAPSHOT.jar
```
