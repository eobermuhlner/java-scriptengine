[![Build Status](https://api.travis-ci.org/eobermuhlner/java-scriptengine.svg?branch=master)](https://travis-ci.org/eobermuhlner/java-scriptengine)
[![Code Coverage](https://badgen.net/codecov/c/github/eobermuhlner/java-scriptengine)](https://codecov.io/gh/eobermuhlner/java-scriptengine)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=java-scriptengine&metric=alert_status)](https://sonarcloud.io/dashboard?id=java-scriptengine)
[![Open Issues](https://badgen.net/github/open-issues/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/issues)
[![Last Commits](https://badgen.net/github/last-commit/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/graphs/commit-activity)
[![Maven Central - java-scriptengine](https://img.shields.io/maven-central/v/ch.obermuhlner/java-scriptengine.svg)](https://search.maven.org/artifact/ch.obermuhlner/java-scriptengine)

# Java ScriptEngine

The `java-scriptengine` (not to be confused with a `javascript` script engine)
compiles and executes `Java` files at runtime.

The script source is a standard Java class that must follow these rules:
* public class `Script` in the default package
* constructor with no arguments (default constructor)
* Callable entry point. One of the following:
   * `Script` implements `Supplier`: the `get()` method is called
   * `Script` implements `Runnable`: the `run()` method is called
   * `Script` has exactly one `public` method with no arguments

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

## Advanced features of `JavaScriptEngine`

The `JavaScriptEngine` has an additional API to control
the execution of the `Script` class.

### Set `ConstructorStrategy` in `JavaScriptEngine` 

You can specify the strategy to construct an actual instance of 
the `Script` class.

The default implementation uses the no-argument default constructor.

```java
public interface ConstructorStrategy {
    Object construct(Class<?> clazz) throws ScriptException;
}
```

This allows to call a constructor that has arguments
or a static constructor method.

### Set `ExecutionStrategyFactory` in `JavaScriptEngine`

You can specify the strategy to execute
the `Script` instance.

The default implementation supports the following:
   * `Script` implements `Supplier`: the `get()` method is called
   * `Script` implements `Runnable`: the `run()` method is called
   * `Script` has exactly one `public` method with no arguments

```java
public interface ExecutionStrategyFactory {
    public ExecutionStrategy create(Class<?> clazz) throws ScriptException;
}
```

```java
public interface ExecutionStrategy {
    Object execute(Object instance) throws ScriptException;
}
```

As a convenience the `MethodExecutionStrategy` is already implemented
and can be used to call a specific method with its arguments.

Use one of the following static constructor methods:
* `MethodExecutionStrategy.byMethod(Method method, Object... arguments)`
* `MethodExecutionStrategy.byMatchingArguments(Class<?> clazz, String methodName, Object... arguments) throws ScriptException`
* `MethodExecutionStrategy.byArgumentTypes(Class<?> clazz, String methodName, Class<?>[] argumentTypes, Object... arguments) throws ScriptException`

The `MethodExecutionStrategy.byMatchingArguments()` is probably
the most convenient way. It determines a matching function
by name and the given arguments (ignoring `null` arguments). 

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

    javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
        return MethodExecutionStrategy.byMatchingArguments(
                clazz,
                "getMessage",
                "Hello", 42);
    });

    Object result = engine.eval("" +
            "public class Script {" +
            "   public String getMessage(Object message, int value) {" +
            "       return \"Message: \" + message + value;" +
            "   } " +
            "}");

    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output shows that `getMessage("Hello", 42)` was called.
```console
Result: Message: Hello42
```

### Set `ExecutionStrategy` in `JavaCompiledScript`

If you compile the script with `Compilable` you can
specify the `ExecutionStrategy` directly on the compiled script
instead of using the `ExecutionStrategyFactory` on the engine.

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

    javaScriptEngine.setExecutionStrategyFactory((clazz) -> {
        return MethodExecutionStrategy.byMatchingArguments(
                clazz,
                "getMessage",
                "Hello", 42);
    });

    JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
            "public class Script {" +
            "   public String getMessage(Object message, int value) {" +
            "       return \"Message: \" + message + value;" +
            "   } " +
            "}");

    compiledScript.setExecutionStrategy(MethodExecutionStrategy.byMatchingArguments(
            compiledScript.getInstanceClass(),
            "getMessage",
            "Hello", 42));

    Object result = compiledScript.eval();

    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```