package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class AutoExecutionStrategy implements ExecutionStrategy {

    private final Method method;

    public AutoExecutionStrategy(Class<?> clazz) {
        method = findCallableMethod(clazz);
    }

    @Override
    public Object execute(Object instance) throws ScriptException {
        if (instance == null) {
            return null;
        }

        if (instance instanceof Supplier) {
            Supplier supplier = (Supplier) instance;
            return supplier.get();
        }

        if (instance instanceof Runnable) {
            Runnable runnable = (Runnable) instance;
            runnable.run();
            return null;
        }

        if (method != null) {
            try {
                return method.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ScriptException(e);
            }
        }

        throw new ScriptException("Cannot execute instance of type: " + instance.getClass());
    }

    private static Method findCallableMethod(Class<?> clazz) {
        List<Method> callableMethods = new ArrayList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() == 0 && (method.getModifiers() & Modifier.PUBLIC) != 0) {
                callableMethods.add(method);
            }
        }

        if (callableMethods.size() == 1) {
            return callableMethods.get(0);
        }

        return null;
    }
}
