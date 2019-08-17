package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class MethodExecutionStrategy implements ExecutionStrategy {
    private Method method;
    private Object[] arguments;

    private MethodExecutionStrategy(Method method, Object... arguments) {
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public Object execute(Object instance) throws ScriptException {
        try {
            return method.invoke(instance, arguments);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ScriptException(e);
        }
    }

    public static MethodExecutionStrategy byMethod(Method method, Object... arguments) {
        return new MethodExecutionStrategy(method, arguments);
    }

    public static MethodExecutionStrategy byArgumentTypes(Class<?> clazz, String methodName, Class<?>[] argumentTypes, Object[] arguments) throws ScriptException {
        try {
            Method method = clazz.getMethod(methodName, argumentTypes);
            return byMethod(method, arguments);
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    private static MethodExecutionStrategy byMatchingArguments(Class<?> clazz, String methodName, Object[] arguments) throws ScriptException {
        List<Method> callableMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if ((method.getModifiers() & Modifier.PUBLIC) != 0) {
                if (matchesArguments(method, arguments)) {
                    callableMethods.add(method);
                }
            }
        }

        if (callableMethods.size() == 0) {
            throw new ScriptException("No method '" + methodName + "' with matching arguments found");
        }
        if (callableMethods.size() > 1) {
            throw new ScriptException("No method '" + methodName + "' with matching arguments found: " + callableMethods.size());
        }

        return byMethod(callableMethods.get(0), arguments);
    }

    private static boolean matchesArguments(Method method, Object[] arguments) {
        Class<?>[] argumentTypes = method.getParameterTypes();
        if (arguments.length != argumentTypes.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] != null && argumentTypes[i].isAssignableFrom(arguments.getClass())) {
                return false;
            }
        }

        return true;
    }
}
