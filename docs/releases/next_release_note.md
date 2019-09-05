# API changes

## Control visible classes of script during execution (Isolation)

In previous releases the classloader of the caller was not visible
to the script during execution.
It was therefore not possible to use classes of the application
from inside the script.

It is now possible to control the isolation level of the script.

* `Isolation.CallerClassLoader`: the script can see the classes of the
  calling application
* `Isolation.IsolatedClassLoader`: the script can only see JDK classes
  and classes declared inside the script

The default behaviour is `Isolation.CallerClassLoader`.


## Renamed to `getCompiledClass()` and `getCompiledInstance()`

Renamed the following methods:
 
* `JavaCompiledScript.getInstanceClass()` to `getCompiledClass()` 
* `JavaCompiledScript.getInstance()` to `getCompiledInstance()` 


## Java Module (Jigsaw)

The java-scriptengine is now a fully compliant Java module.

```
module ch.obermuhlner.scriptengine.java {
    exports ch.obermuhlner.scriptengine.java;
    exports ch.obermuhlner.scriptengine.java.constructor;
    exports ch.obermuhlner.scriptengine.java.execution;
    exports ch.obermuhlner.scriptengine.java.name;
    exports ch.obermuhlner.scriptengine.java.util;

    requires transitive java.scripting;

    requires java.compiler;
}
```

The OSGi `Export-Package` declaration in the `MANIFEST.MF` exports the
same packages.


# Bugfixes

## Fix javadoc in `MethodExecutionStrategy` `byArgumentTypes()` and `byMatchingArguments()`

The javadoc for the methods
* `MethodExecutionStrategy.byArgumentTypes()`
* `MethodExecutionStrategy.byMatchingArguments()`

described the return value wrong.


# Enhancements

## Added `MethodExecutionStrategy.byMainMethod()`

Added `MethodExecutionStrategy.byMainMethod()` that will call the `public static void main(String[] args)`
with the specified arguments. 


# Examples

Note: The example code is available on github, but not part of the
`java-scriptengine` library.

All of the example code in this documentation is runnable
in the class `ScriptEngineExample`.

## Added `ScriptEnginePerformance`

An example class `ScriptEnginePerformance` was added to measure the
performance of the `JavaScriptEngine` for compilation and evaluation
of scripts.
