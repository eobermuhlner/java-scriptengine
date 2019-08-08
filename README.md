[![Build Status](https://travis-ci.org/eobermuhlner/java-scriptengine.svg?branch=master)](https://travis-ci.org/eobermuhlner/java-scriptengine)
[![Code Coverage](https://img.shields.io/codecov/c/github/eobermuhlner/java-scriptengine.svg)](https://codecov.io/gh/eobermuhlner/java-scriptengine)
[![Dependencies](https://img.shields.io/librariesio/github/eobermuhlner/java-scriptengine.svg)](https://github.com/eobermuhlner/java-scriptengine/pulse)
[![Maven Central](https://img.shields.io/maven-central/v/ch.obermuhlner/java-scriptengine.svg)](https://search.maven.org/artifact/ch.obermuhlner/java-scriptengine)

# java-scriptengine

A collection of JSR-223 compatible script engines for Java.

Currently supported script engines are:
* JShell


## JShell scripting engine

The JShell was added to Java 9 and was designed to be used for interactive execution of code snippets in Java.



The following code snippet shows a simple usage of the JShell script engine:
```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("jshell");

    String script = "" +
            "System.out.println(\"Input A: \" + inputA);" +
            "System.out.println(\"Input B: \" + inputB);" +
            "var output = inputA + inputB;" +
            "1000 + output;";

    engine.put("inputA", 2);
    engine.put("inputB", 3);

    Object result = engine.eval(script);
    System.out.println("Result: " + result);

    Object output = engine.get("output");
    System.out.println("Output Variable: " + output);

} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output of this snippet shows that the bindings for input and output variables are working correctly.
The return value of the JShell script is the value of the last statement `1000 + output`.
```console
Input A: 2
Input B: 3
Result: 1005
Output Variable: 5
```

### Access to classes

The JShell script is executed in the same process 
and has therefore access to the same classes.

Assume your project has the following class:
```java
package ch.obermuhlner.scriptengine.example;

public class Person {
    public String name;
    public int birthYear;

    @Override
    public String toString() {
        return "Person{name=" + name + ", birthYear=" + birthYear + "}";
    }
}
```

In this case you can run a JShell script that uses this class `Person`:
```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("jshell");

    String script = "" +
            "import ch.obermuhlner.scriptengine.example.Person;" +
            "var person = new Person();" +
            "person.name = \"Eric\";" +
            "person.birthYear = 1967;";

    Object result = engine.eval(script);
    System.out.println("Result: " + result);

    Object person = engine.get("person");
    System.out.println("Person Variable: " + person);

} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output of this snippet shows that the variable `person` created inside the JShell script is now available in the calling Java:
```console
Result: 1967
Person Variable: Person{name=Eric, birthYear=1967}
```

### Error handling

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("jshell");

    String script = "" +
            "System.out.println(unknown);" +
            "var message = \"Should never reach this point\"";

    Object result = engine.eval(script);
    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output of this snippet shows that the variable `unknown` cannot be found:
```console
javax.script.ScriptException: cannot find symbol
  symbol:   variable unknown
  location: class 
System.out.println(unknown);
	at ch.obermuhlner.scriptengine.jshell.JShellScriptEngine.evaluateScript(JShellScriptEngine.java:216)
	at ch.obermuhlner.scriptengine.jshell.JShellScriptEngine.eval(JShellScriptEngine.java:98)
	at ch.obermuhlner.scriptengine.jshell.JShellScriptEngine.eval(JShellScriptEngine.java:84)
	at ch.obermuhlner.scriptengine.jshell.JShellScriptEngine.eval(JShellScriptEngine.java:74)
	at ch.obermuhlner.scriptengine.example.ScriptEngineExample.runErrorExample(ScriptEngineExample.java:84)
	at ch.obermuhlner.scriptengine.example.ScriptEngineExample.main(ScriptEngineExample.java:14)
```
