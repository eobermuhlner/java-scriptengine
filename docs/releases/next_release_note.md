# API changes

No API changes.


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
