![Repo Closed](https://badgen.net/badge/repo/closed/red)
[![Goto jshell-scriptengine](https://badgen.net/badge/goto/jshell-scriptengine/blue)](https://github.com/eobermuhlner/jshell-scriptengine)
[![Goto spel-scriptengine](https://badgen.net/badge/goto/spel-scriptengine/blue)](https://github.com/eobermuhlner/spel-scriptengine)
[![Goto scriptengine-utils](https://badgen.net/badge/goto/scriptengine-utils/blue)](https://github.com/eobermuhlner/scriptengine-utils)

This project has been split into several github repositories:
- https://github.com/eobermuhlner/jshell-scriptengine
- https://github.com/eobermuhlner/spel-scriptengine
- https://github.com/eobermuhlner/scriptengine-utils

No more commits or merge requests are accepted on this repository.

## Using JShell scripting engine in your projects 

The first library published from this repository `scriptengine-jshell` version
was renamed to `jshell-scriptengine`!

To use the JShell scripting you can either download the newest version of the .jar file from the
[published releases on Github](https://github.com/eobermuhlner/jshell-scriptengine/releases/)
or use the following dependency to
[Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cjshell-scriptengine)
in your build script (please verify the version number to be the newest release):

### Use JShell scripting engine in Maven build

```xml
<dependency>
  <groupId>ch.obermuhlner</groupId>
  <artifactId>jshell-scriptengine</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Use JShell scripting engine in Gradle build

```gradle
repositories {
  mavenCentral()
}

dependencies {
  compile 'ch.obermuhlner:jshell-scriptengine:1.0.0'
}
```
