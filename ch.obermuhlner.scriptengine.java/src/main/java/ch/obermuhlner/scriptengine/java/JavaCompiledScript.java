package ch.obermuhlner.scriptengine.java;

import javax.script.*;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class JavaCompiledScript extends CompiledScript {
    private JavaScriptEngine engine;
    private Object instance;
    private ExecutionStrategy executionStrategy;

    JavaCompiledScript(JavaScriptEngine engine, Object instance) {
        this(engine, instance, new AutoExecutionStrategy());
    }

    JavaCompiledScript(JavaScriptEngine engine, Object instance, ExecutionStrategy executionStrategy) {
        this.engine = engine;
        this.instance = instance;
        this.executionStrategy = executionStrategy;
    }

    @Override
    public ScriptEngine getEngine() {
        return engine;
    }

    @Override
    public Object eval(ScriptContext context) throws ScriptException {
        Bindings globalBindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        Bindings engineBindings = context.getBindings(ScriptContext.ENGINE_SCOPE);

        return executionStrategy.execute(instance);
    }

    public interface ExecutionStrategy {
        Object execute(Object instance) throws ScriptException;
    }

    public static class AutoExecutionStrategy implements ExecutionStrategy {
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

            Class<?> clazz = instance.getClass();
            List<Method> callableMethods = new ArrayList<>();
            for (Method method : clazz.getDeclaredMethods()) {
                if (method.getParameterCount() == 0 && (method.getModifiers() & Modifier.PUBLIC) != 0) {
                    callableMethods.add(method);
                }
            }

            if (callableMethods.size() == 1) {
                Method method = callableMethods.get(0);
                try {
                    Object result = method.invoke(instance);
                    return result;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new ScriptException(e);
                }
            }

            return new ScriptException("Cannot execute instance: " + clazz);
        }
    }
}
