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

## Special `codeBase` for permission policy

The script classes are executed using a special `codeBase`: 
`jrt:/ch.obermuhlner.scriptengine.java/memory-class` 

This allows to grant specific permissions to the script classes.

Here an example policy file: 
```
// global permissions (for the application and on-the-fly compiled script classes)
grant {
  permission java.io.FilePermission "<<ALL FILES>>", "read";
};

// permissions for the example application
grant codeBase "file:/C:/Users/obe/git/java-scriptengine/ch.obermuhlner.scriptengine.example/out/production/classes/" {
  permission java.lang.RuntimePermission "accessDeclaredMembers";
  permission java.lang.RuntimePermission "accessSystemModules";
  permission java.lang.RuntimePermission "closeClassLoader";
  permission java.lang.RuntimePermission "createClassLoader";
  permission java.util.PropertyPermission "application.home", "read";
  permission java.util.PropertyPermission "env.class.path", "read";
  permission java.util.PropertyPermission "java.class.path", "read";
  permission java.util.PropertyPermission "java.home", "read";
};

// permissions for the java-scriptengine
grant codeBase "file:/C:/Users/obe/git/java-scriptengine/ch.obermuhlner.scriptengine.java/out/production/classes/" {
  permission java.lang.RuntimePermission "accessDeclaredMembers";
  permission java.lang.RuntimePermission "accessSystemModules";
  permission java.lang.RuntimePermission "closeClassLoader";
  permission java.lang.RuntimePermission "createClassLoader";
  permission java.util.PropertyPermission "application.home", "read";
  permission java.util.PropertyPermission "env.class.path", "read";
  permission java.util.PropertyPermission "java.class.path", "read";
  permission java.util.PropertyPermission "java.home", "read";
  permission java.lang.RuntimePermission "exitVM";
};

// permissions for on-the-fly compiled script classes (notice the special URL)
grant codeBase "jrt:/ch.obermuhlner.scriptengine.java/memory-class" {
  permission java.lang.RuntimePermission "exitVM";
  permission java.util.PropertyPermission "java.home", "read";
};

// permissions for the jdk.compiler module
grant codeBase "jrt:/jdk.compiler" {
  permission java.lang.RuntimePermission "closeClassLoader";
  permission java.lang.RuntimePermission "createClassLoader";
  permission java.util.PropertyPermission "application.home", "read";
  permission java.util.PropertyPermission "env.class.path", "read";
  permission java.util.PropertyPermission "java.class.path", "read";
  permission java.util.PropertyPermission "java.home", "read";
};
```


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
