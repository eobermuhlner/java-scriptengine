[![Build Status](https://api.travis-ci.org/eobermuhlner/java-scriptengine.svg?branch=master)](https://travis-ci.org/eobermuhlner/java-scriptengine)
[![Code Coverage](https://badgen.net/codecov/c/github/eobermuhlner/java-scriptengine)](https://codecov.io/gh/eobermuhlner/java-scriptengine)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=java-scriptengine&metric=alert_status)](https://sonarcloud.io/dashboard?id=java-scriptengine)
[![Open Issues](https://badgen.net/github/open-issues/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/issues)
[![Last Commits](https://badgen.net/github/last-commit/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/graphs/commit-activity)
[![Maven Central - java-scriptengine](https://img.shields.io/maven-central/v/ch.obermuhlner/java-scriptengine.svg)](https://search.maven.org/artifact/ch.obermuhlner/java-scriptengine)

# Java ScriptEngine

The `java-scriptengine` (not to be confused with a `javascript` script engine)
compiles and executes `Java` files at runtime.

# Project State

This project is NOT YET PRODUCTION READY!

Several issues must be solved before this project can be used in a 
production environment.

Known issues:
* Java script class must be called `Script`
* not possible to access classes of the caller

## Simple usage

The following code snippet shows a simple usage of the Java script engine:
```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");

    Object result = engine.eval("" +
            "public class Script {" +
            "   public String getMessage() {" +
            "       return \"Hello World\";" +
            "   } " +
            "}");
    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output shows the result of the only method in the `Script` class.
```console
Result: Hello World
```

## Compiling

Calling `ScriptEngine.eval()` multiple times is very efficient¨because
the same script has to be compiled every time.

The `JavaScriptEngine` implements the `Compilable` interface which
allows to compile the script once and run it multiple times.

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    Compilable compiler = (Compilable) engine;

    CompiledScript compiledScript = compiler.compile("" +
            "public class Script {" +
            "   private int counter = 1;" +
            "   public String getMessage() {" +
            "       return \"Hello World #\" + counter++;" +
            "   } " +
            "}");

    Object result1 = compiledScript.eval();
    System.out.println("Result1: " + result1);

    Object result2 = compiledScript.eval();
    System.out.println("Result2: " + result2);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output shows that the same instance was called
multiple times (without recompiling the script).
```console
Result1: Hello World #1
Result2: Hello World #2
```

## Bindings instance variables (fields) 

You can read and write variables, both instance variables (fields) and static variables,
by using `Bindings` in the script engine.

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    Compilable compiler = (Compilable) engine;

    CompiledScript compiledScript = compiler.compile("" +
            "public class Script {" +
            "   public static String message = \"Counting\";" +
            "   public int counter = 1;" +
            "   public String getMessage() {" +
            "       return message + \" #\" + counter++;" +
            "   } " +
            "}");

    {
        Bindings bindings = engine.createBindings();

        Object result = compiledScript.eval(bindings);

        System.out.println("Result1: " + result);
        System.out.println("Variable1 message: " + bindings.get("message"));
        System.out.println("Variable1 counter: " + bindings.get("counter"));
    }

    {
        Bindings bindings = engine.createBindings();
        bindings.put("message", "Hello world");

        Object result = compiledScript.eval(bindings);

        System.out.println("Result2: " + result);
        System.out.println("Variable2 message: " + bindings.get("message"));
        System.out.println("Variable2 counter: " + bindings.get("counter"));
    }
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output shows that bindings can read and write values
from both instance and static variables of your class. 
```console
Result1: Counting #1
Variable1 message: Counting
Variable1 counter: 2
Result2: Hello world #2
Variable2 message: Hello world
Variable2 counter: 3
```
