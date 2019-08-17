package ch.obermuhlner.scriptengine.java;

import ch.obermuhlner.scriptengine.java.execution.AutoExecutionStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategy;

import javax.script.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

public class JavaCompiledScript extends CompiledScript {
    private JavaScriptEngine engine;
    private Object instance;
    private ExecutionStrategy executionStrategy;

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

        pushVariables(globalBindings, true);
        pushVariables(engineBindings, false);

        Object result = executionStrategy.execute(instance);

        pullVariables(globalBindings, true);
        pullVariables(engineBindings, false);

        return result;
    }

    private void pushVariables(Bindings bindings, boolean staticVariables) throws ScriptException {
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            Class<?> clazz = instance.getClass();
            try {
                Field field = clazz.getField(name);
                if (Modifier.isStatic(field.getModifiers()) == staticVariables) {
                    field.set(instance, value);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ScriptException(e);
            }
        }
    }

    private void pullVariables(Bindings bindings, boolean staticVariables) throws ScriptException {
        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) == staticVariables) {
                try {
                    String name = field.getName();
                    Object value = field.get(instance);
                    bindings.put(name, value);
                } catch (IllegalAccessException e) {
                    throw new ScriptException(e);
                }
            }
        }
    }
}
