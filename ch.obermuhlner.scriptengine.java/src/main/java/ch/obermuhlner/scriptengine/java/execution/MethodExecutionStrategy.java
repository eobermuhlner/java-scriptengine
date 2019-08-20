package ch.obermuhlner.scriptengine.java.execution;

import ch.obermuhlner.scriptengine.java.internal.ReflectionUtil;

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

    public static MethodExecutionStrategy byArgumentTypes(Class<?> clazz, String methodName, Class<?>[] argumentTypes, Object... arguments) throws ScriptException {
        try {
            Method method = clazz.getMethod(methodName, argumentTypes);
            return byMethod(method, arguments);
        } catch (NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    public static MethodExecutionStrategy byMatchingArguments(Class<?> clazz, String methodName, Object... arguments) throws ScriptException {
        List<Method> matchingMethods = new ArrayList<>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)
                    && (method.getModifiers() & Modifier.PUBLIC) != 0
                    && ReflectionUtil.matchesArguments(method, arguments)) {
                matchingMethods.add(method);
            }
        }

        int count = matchingMethods.size();
        if (count == 0) {
            throw new ScriptException("No method '" + methodName + "' with matching arguments found");
        } else if (count > 1) {
            throw new ScriptException("Ambiguous methods '" + methodName + "' with matching arguments found: " + count);
        }

        return byMethod(matchingMethods.get(0), arguments);
    }

}
