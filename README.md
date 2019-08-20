[![Build Status](https://api.travis-ci.org/eobermuhlner/java-scriptengine.svg?branch=master)](https://travis-ci.org/eobermuhlner/java-scriptengine)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=java-scriptengine&metric=coverage)](https://sonarcloud.io/dashboard?id=java-scriptengine)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=java-scriptengine&metric=alert_status)](https://sonarcloud.io/dashboard?id=java-scriptengine)
[![Open Issues](https://badgen.net/github/open-issues/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/issues)
[![Last Commits](https://badgen.net/github/last-commit/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/graphs/commit-activity)
[![Maven Central - java-scriptengine](https://img.shields.io/maven-central/v/ch.obermuhlner/java-scriptengine.svg)](https://search.maven.org/artifact/ch.obermuhlner/java-scriptengine)

# Java ScriptEngine

The `java-scriptengine` (not to be confused with a `javascript` script engine)
compiles and executes `Java` files at runtime.

The script source is a standard Java class that must follow these rules:
* public class
* constructor with no arguments (default constructor)
* Callable entry point. One of the following:
   * class implements `Supplier`: the `get()` method is called
   * class implements `Runnable`: the `run()` method is called
   * class has exactly one `public` method with no arguments: call it

The script class can be arbitrarily named and may be in a named package or the default package.

Note: The scanner that parses the script for package and class names is very simple.
Avoid confusing it with comments that contain the keywords `package` or `public class`
or comments between the keywords and the package/class names.

## Using Java scripting engine in your projects 

To use the JShell scripting you can either download the newest version of the .jar file from the
[published releases on Github](https://github.com/eobermuhlner/java-scriptengine/releases/)
or use the following dependency to
[Maven Central](https://search.maven.org/#search%7Cga%7C1%7Cjava-scriptengine)
in your build script (please verify the version number to be the newest release):

### Use Java scripting engine in Maven build

```xml
<dependency>
  <groupId>ch.obermuhlner</groupId>
  <artifactId>java-scriptengine</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Use Java scripting engine in Gradle build

```gradle
repositories {
  mavenCentral()
}

dependencies {
  compile 'ch.obermuhlner:java-scriptengine:1.0.0'
}
```

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

Calling `ScriptEngine.eval()` multiple times is very efficient because
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

## Binding variables 

You can read and write `public` variables, both instance variables (fields) and static variables,
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
the creation and execution of the script class.

### Set `NameStrategy` in `JavaScriptEngine` 

You can specify the strategy to determine the name of the script class
from the script.

```java
public interface NameStrategy {
    String getFullName(String script) throws ScriptException;
}
```

The default implementation `DefaultNameStrategy` uses a simple
(regular expression based) scanner
to find the package name and the class name in the script.

Alternatively the `FixNameStrategy` allows to set an explicit
fully qualified class name. 

### Set `ConstructorStrategy` in `JavaScriptEngine` 

You can specify the strategy to construct an actual instance of 
the script class.

```java
public interface ConstructorStrategy {
    Object construct(Class<?> clazz) throws ScriptException;
}
```

The default implementation `DefaultConstructorStrategy`
uses the no-argument default constructor.

Additional static constructor methods in `DefaultConstructorStrategy`
allow to use a constructor with explicit arguments.

The following example uses the
convenience `DefaultConstructorStrategy.byMatchingArguments()`
to to determine a matching constructor
using the given arguments:
```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

    javaScriptEngine.setConstructorStrategy(DefaultConstructorStrategy.byMatchingArguments("Hello", 42));

    Object result = engine.eval("" +
            "public class Script {" +
            "   private final String message;" +
            "   private final int value;" +
            "   public Script(String message, int value) {" +
            "       this.message = message;" +
            "       this.value = value;" +
            "   }" +
            "   public String getMessage() {" +
            "       return \"Message: \" + message + value;" +
            "   }" +
            "}");

    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The matching algorithm in `DefaultConstructorStrategy.byMatchingArguments()`
will ignore `null` arguments for non-primitive argument types.

If the algorithm finds more than one matching constructor a `ScriptException` is thrown.


### Set `ExecutionStrategyFactory` in `JavaScriptEngine`

You can specify the strategy to execute the script class instance
by providing a factory that creates an `ExecutionStrategy`
from a `Class<?>`.

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

The default implementation `DefaultExecutionStrategy` supports the following:
   * class implements `Supplier`: the `get()` method is called
   * class implements `Runnable`: the `run()` method is called
   * class has exactly one `public` method with no arguments: call it

Alternatively the `MethodExecutionStrategy`
can be used to call a specific method with its arguments.

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

The matching algorithm in `MethodExecutionStrategy.byMatchingArguments()`
will ignore `null` arguments for non-primitive argument types.

If the algorithm finds more than one matching method a `ScriptException` is thrown.

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
