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

## Added `Example.method(y)`

Added `Example.method(y)` 


# Examples

Note: The example code is available on github, but not part of the
`java-scriptengine` library.

No changes in the examples.
