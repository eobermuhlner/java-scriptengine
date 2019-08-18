package ch.obermuhlner.scriptengine.java;

import ch.obermuhlner.scriptengine.java.execution.AutoExecutionStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategy;

import javax.script.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
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

        pushVariables(globalBindings, engineBindings);
        Object result = executionStrategy.execute(instance);
        pullVariables(globalBindings, engineBindings);

        return result;
    }

    private void pushVariables(Bindings globalBindings, Bindings engineBindings) throws ScriptException {
        Map<String, Object> mergedBindings = mergeBindings(globalBindings, engineBindings);

        for (Map.Entry<String, Object> entry : mergedBindings.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            Class<?> clazz = instance.getClass();
            try {
                Field field = clazz.getField(name);
                field.set(instance, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ScriptException(e);
            }
        }
    }

    private void pullVariables(Bindings globalBindings, Bindings engineBindings) throws ScriptException {
        Class<?> clazz = instance.getClass();

        for (Field field : clazz.getFields()) {
            try {
                String name = field.getName();
                Object value = field.get(instance);
                setBindingsValue(globalBindings, engineBindings, name, value);
            } catch (IllegalAccessException e) {
                throw new ScriptException(e);
            }
        }
    }

    private void setBindingsValue(Bindings globalBindings, Bindings engineBindings, String name, Object value) {
        if (!engineBindings.containsKey(name) && globalBindings.containsKey(name)) {
            globalBindings.put(name, value);
        } else {
            engineBindings.put(name, value);
        }
    }

    private Map<String, Object> mergeBindings(Bindings... bindingsToMerge) {
        Map<String, Object> variables = new HashMap<>();

        for (Bindings bindings : bindingsToMerge) {
            if (bindings != null) {
                for (Map.Entry<String, Object> globalEntry : bindings.entrySet()) {
                    variables.put(globalEntry.getKey(), globalEntry.getValue());
                }
            }
        }

        return variables;
    }
}
