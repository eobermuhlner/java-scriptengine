[![Build Status](https://api.travis-ci.org/eobermuhlner/java-scriptengine.svg?branch=master)](https://travis-ci.org/eobermuhlner/java-scriptengine)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=java-scriptengine&metric=coverage)](https://sonarcloud.io/dashboard?id=java-scriptengine)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=java-scriptengine&metric=alert_status)](https://sonarcloud.io/dashboard?id=java-scriptengine)
[![Open Issues](https://badgen.net/github/open-issues/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/issues)
[![Last Commits](https://badgen.net/github/last-commit/eobermuhlner/java-scriptengine)](https://github.com/eobermuhlner/java-scriptengine/graphs/commit-activity)
[![Maven Central - java-scriptengine](https://img.shields.io/maven-central/v/ch.obermuhlner/java-scriptengine.svg)](https://search.maven.org/artifact/ch.obermuhlner/java-scriptengine) [![Join the chat at https://gitter.im/java-scriptengine/community](https://badges.gitter.im/java-scriptengine/community.svg)](https://gitter.im/java-scriptengine/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Java ScriptEngine

The `java-scriptengine` (not to be confused with a `javascript` script engine)
compiles and executes `Java` files at runtime.

The script source is a standard Java class that out of the box must follow these rules:
* public class
* constructor with no arguments (default constructor)
* Callable entry point. One of the following:
   * class implements `Supplier`: the `get()` method is called
   * class implements `Runnable`: the `run()` method is called
   * class has exactly one `public` method with no arguments: call it<br>
     Note: The class may have any number `private` methods 
     and `public` methods with arguments.

The script class can be arbitrarily named and may be in a named package or the default package.

The `java-scriptengine` needs Java 11 or later to run.

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
  <version>1.0.1</version>
</dependency>
```

### Use Java scripting engine in Gradle build

```gradle
repositories {
  mavenCentral()
}

dependencies {
  compile 'ch.obermuhlner:java-scriptengine:1.0.1'
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

While example was written to be the simplest and easiest way to run
a Java script, it is *not* the best way! Please have a look at
the next chapter before you start integrating it into your application.  


## Compiling

Calling `ScriptEngine.eval()` multiple times is very inefficient because
the same script has to be compiled every time.

The `JavaScriptEngine` implements the `Compilable` interface which
allows to compile the script once and run it multiple times.

The following example also shows the recommended best practices for writing scripts:
* declare a package (to avoid the unnamed package)
* script class implements `java.util.function.Supplier<String>`
  (to avoid ambiguities on the method to call)
   

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    Compilable compiler = (Compilable) engine;

    CompiledScript compiledScript = compiler.compile("" +
            "package script;" +
            "public class Script implements java.util.function.Supplier<String> {" +
            "   private int counter = 1;" +
            "   @Override" +
            "   public String get() {" +
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

Separating the compilation from the evaluation is more efficient if you
need to evaluate the same script multiple times.

Here the execution times in milliseconds for:
* Multi Eval
  * many calls to `JavaScriptEngine.eval(String)`
    (essentially compile and evaluate every time)
* Compile + Multi Eval
  * single call to `JavaScriptEngine.compile(String)`
  * many calls to `JavaCompiledScript.eval(Bindings)`

![Performance: Compile Multiple Evaluations](docs/performance/Compile_Multiple_Evaluations.svg)

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

## Using application classes from script

The script can see classes of the calling application.

Assume that the calling application declares the following class:

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

Your script has access to the public class declared in the calling application:
```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

    JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
            "import ch.obermuhlner.scriptengine.example.Person;" +
            "public class Script {" +
            "   public Person getPerson() {" +
            "       Person person = new Person();" +
            "       person.name = \"Eric\";" +
            "       person.birthYear = 1967;" +
            "       return person;" +
            "   } " +
            "}");

    Object result = compiledScript.eval();
    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output shows that the script was able to use class `Person`.
```console
Result: Person{name=Eric, birthYear=1967}
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
            compiledScript.getCompiledClass(),
            "getMessage",
            "Hello", 42));

    Object result = compiledScript.eval();

    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

### Set `Isolation` in `JavaScriptEngine`

You can specfy the `Isolation` level of the script.

* `Isolation.CallerClassLoader`: the script can see the classes of the
  calling application
* `Isolation.IsolatedClassLoader`: the script can only see JDK classes
  and classes declared inside the script

The default behaviour is `Isolation.CallerClassLoader`.


### Special `codeBase` for permission policy

The script classes are executed using a special `codeBase`: 
`jrt:/ch.obermuhlner.scriptengine.java/memory-class` 

This allows to grant specific permissions to the script classes.

Here an example policy file `example.policy` (you will need to edit the file path to the application classes in your installation): 
```
// global permissions (for the application and on-the-fly compiled script classes)
grant {
  permission java.io.FilePermission "<<ALL FILES>>", "read";

  permission java.util.PropertyPermission "application.home", "read";
  permission java.util.PropertyPermission "env.class.path", "read";
  permission java.util.PropertyPermission "java.class.path", "read";
  permission java.util.PropertyPermission "java.home", "read";
};

// permissions for the example application
grant codeBase "file:/C:/Users/obe/git/java-scriptengine/ch.obermuhlner.scriptengine.example/out/production/classes/" {
  permission java.lang.RuntimePermission "accessDeclaredMembers";
  permission java.lang.RuntimePermission "accessSystemModules";
  permission java.lang.RuntimePermission "closeClassLoader";
  permission java.lang.RuntimePermission "createClassLoader";
};

// permissions for the java-scriptengine
grant codeBase "file:/C:/Users/obe/git/java-scriptengine/ch.obermuhlner.scriptengine.java/out/production/classes/" {
  permission java.lang.RuntimePermission "accessDeclaredMembers";
  permission java.lang.RuntimePermission "accessSystemModules";
  permission java.lang.RuntimePermission "closeClassLoader";
  permission java.lang.RuntimePermission "createClassLoader";
};

// permissions for the jdk.compiler module
grant codeBase "jrt:/jdk.compiler" {
  permission java.lang.RuntimePermission "closeClassLoader";
  permission java.lang.RuntimePermission "createClassLoader";
};

// permissions for on-the-fly compiled script classes (notice the special URL)
grant codeBase "jrt:/ch.obermuhlner.scriptengine.java/memory-class" {
  permission java.lang.RuntimePermission "exitVM";
};
```

Add the following VM arguments to your application launch:
`-Djava.security.manager -Djava.security.policy=path/to/example.policy`

The permission given to the `jrt:/ch.obermuhlner.scriptengine.java/memory-class`
allow to execute the dangerous `System.exit()` in the following example:

```java
try {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("java");
    JavaScriptEngine javaScriptEngine = (JavaScriptEngine) engine;

    javaScriptEngine.setIsolation(Isolation.IsolatedClassLoader);

    JavaCompiledScript compiledScript = javaScriptEngine.compile("" +
            "import ch.obermuhlner.scriptengine.example.Person;" +
            "public class Script {" +
            "   public Object getPerson() {" +
            "       System.out.println(\"Calling System.exit(111)\");" +
            "       System.exit(111);" +
            "       return 123;" +
            "   } " +
            "}");

    Object result = compiledScript.eval();
    System.out.println("Result: " + result);
} catch (ScriptException e) {
    e.printStackTrace();
}
```

The console output shows that the entire application exited before printing out the result.
```console
Calling System.exit(111)
```

Remove the granted permission for `jrt:/ch.obermuhlner.scriptengine.java/memory-class`
in the `example.policy` file to prohibit the dangerous `System.exit()`.

```console
Calling System.exit(111)
javax.script.ScriptException: java.lang.reflect.InvocationTargetException
	at ch.obermuhlner.scriptengine.java.execution.DefaultExecutionStrategy.execute(DefaultExecutionStrategy.java:52)
	at ch.obermuhlner.scriptengine.java.JavaCompiledScript.eval(JavaCompiledScript.java:76)
	at java.scripting/javax.script.CompiledScript.eval(CompiledScript.java:103)
	at ch.obermuhlner.scriptengine.example.ScriptEngineExample.runDangerousCode2Example(ScriptEngineExample.java:339)
	at ch.obermuhlner.scriptengine.example.ScriptEngineExample.runExamples(ScriptEngineExample.java:27)
	at ch.obermuhlner.scriptengine.example.ScriptEngineExample.main(ScriptEngineExample.java:13)
Caused by: java.lang.reflect.InvocationTargetException
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:566)
	at ch.obermuhlner.scriptengine.java.execution.DefaultExecutionStrategy.execute(DefaultExecutionStrategy.java:50)
	... 5 more
Caused by: java.security.AccessControlException: access denied ("java.lang.RuntimePermission" "exitVM.111")
	at java.base/java.security.AccessControlContext.checkPermission(AccessControlContext.java:472)
	at java.base/java.security.AccessController.checkPermission(AccessController.java:897)
	at java.base/java.lang.SecurityManager.checkPermission(SecurityManager.java:322)
	at java.base/java.lang.SecurityManager.checkExit(SecurityManager.java:534)
	at java.base/java.lang.Runtime.exit(Runtime.java:113)
	at java.base/java.lang.System.exit(System.java:1746)
	at Script.getPerson(Script.java:1)
	... 10 more
```

