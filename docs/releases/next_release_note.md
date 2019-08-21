# API changes

## `ConstructorStrategy` may return `null` to run `static` methods

The `ConstructorStrategy` is now allowed to return a `null` instance.
This indicates that a static method should be called in the
`ExecutionStrategy`. 

The `DefaultExecutionStrategy` and `MethodExecutionStrategy` support this behaviour.

Using a `NullConstructorStrategy` is the most convenient way to do this.


# Bugfixes

## `MethodExecutionStrategy.byMatchingArguments()` did not check `methodName`

MethodExecutionStrategy.byMatchingArguments() is supposed to search for
a method with the specified methodName, but the name was actually ignored.


# Enhancements

## Improved error messages for ambiguous constructors and methods 

The error messages for ambiguous constructors and methods in the
`ScriptException`s thrown by the `DefaultConstructorStrategy`
and the `MethodExecutionStrategy` have been improved to list the
ambiguous constructors and methods.

``` console
Ambiguous constructors with matching arguments found:
public ch.obermuhlner.scriptengine.java.constructor.DefaultConstructorStrategyTest$TestConstructor(java.lang.String,java.lang.Long,int)
public ch.obermuhlner.scriptengine.java.constructor.DefaultConstructorStrategyTest$TestConstructor(java.lang.String,java.lang.String,int)
```

``` console
Ambiguous methods 'doSomething' with matching arguments found:
public java.lang.String ch.obermuhlner.scriptengine.java.execution.MethodExecutionStrategyTest$TestMethod.doSomething(java.lang.String,java.lang.Long,int)
public java.lang.String ch.obermuhlner.scriptengine.java.execution.MethodExecutionStrategyTest$TestMethod.doSomething(java.lang.String,java.lang.String,int)
```

# Examples

Note: The example code is available on github, but not part of the
`java-scriptengine` library.

No changes in the examples.
