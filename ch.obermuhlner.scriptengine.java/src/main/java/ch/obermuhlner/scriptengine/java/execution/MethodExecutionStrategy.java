package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static MethodExecutionStrategy byArgumentTypes(Class<?> clazz, String methodName, Class<?>[] argumentTypes, Object... arguments) throws ScriptException {
        try {
            Method method = clazz.getMethod(methodName, argumentTypes);
            return byMethod(method, arguments);
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    public static MethodExecutionStrategy byMatchingArguments(Class<?> clazz, String methodName, Object... arguments) throws ScriptException {
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
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (arguments.length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                if (parameterTypes[i].isPrimitive()) {
                    return false;
                }
            } else {
                Class<?> argumentType = arguments[i].getClass();
                if (!matchesType(parameterTypes[i], argumentType)) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean matchesType(Class<?> parameterType, Class<?> argumentType) {
        if ((parameterType == int.class && argumentType == Integer.class) ||
                (parameterType == long.class && argumentType == Long.class) ||
                (parameterType == short.class && argumentType == Short.class) ||
                (parameterType == byte.class && argumentType == Byte.class) ||
                (parameterType == boolean.class && argumentType == Boolean.class) ||
                (parameterType == float.class && argumentType == Float.class) ||
                (parameterType == double.class && argumentType == Double.class) ||
                (parameterType == char.class && argumentType == Character.class)) {
            return true;
        }
        return parameterType.isAssignableFrom(argumentType);
    }
}
