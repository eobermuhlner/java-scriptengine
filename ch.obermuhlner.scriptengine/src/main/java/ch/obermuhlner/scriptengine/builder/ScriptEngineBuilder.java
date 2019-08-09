package ch.obermuhlner.scriptengine.builder;

import javax.script.*;
import java.io.Reader;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScriptEngineBuilder {
    protected final ScriptEngine engine;

    public static ScriptEngineBuilder byEngine(ScriptEngine engine) {
        return new ScriptEngineBuilder(engine);
    }

    public static ScriptEngineBuilder byName(String name) {
        ScriptEngineManager manager = new ScriptEngineManager();
        return byName(manager, name);
    }

    public static ScriptEngineBuilder byName(ScriptEngineManager manager, String name) {
        return new ScriptEngineBuilder(manager.getEngineByName(name));
    }

    public static ScriptEngineBuilder byExtension(String extension) {
        ScriptEngineManager manager = new ScriptEngineManager();
        return byExtension(manager, extension);
    }

    public static ScriptEngineBuilder byExtension(ScriptEngineManager manager, String extension) {
        return new ScriptEngineBuilder(manager.getEngineByName(extension));
    }

    public static ScriptEngineBuilder byMimeType(String mimeType) {
        ScriptEngineManager manager = new ScriptEngineManager();
        return byExtension(manager, mimeType);
    }

    public static ScriptEngineBuilder byMimeType(ScriptEngineManager manager, String mimeType) {
        return new ScriptEngineBuilder(manager.getEngineByMimeType(mimeType));
    }

    private ScriptEngineBuilder(ScriptEngine engine) {
        Objects.requireNonNull(engine);
        this.engine = engine;
    }

    public ScriptEngineBuilder setContext(ScriptContext context) {
        engine.setContext(context);
        return this;
    }

    public ScriptEngineBuilder onContext(Consumer<ScriptContext> contextConsumer) {
        contextConsumer.accept(engine.getContext());
        return this;
    }

    public ScriptEngineBuilder setBindings(Bindings bindings, int scope) {
        engine.setBindings(bindings, scope);
        return this;
    }

    public ScriptEngineBuilder setBindings(int scope, Consumer<Bindings> bindingsConsumer) {
        Bindings bindings = engine.createBindings();
        bindingsConsumer.accept(bindings);
        return setBindings(bindings, scope);
    }

    public ScriptEngineBuilder onBindings(int scope, Consumer<Bindings> bindingsConsumer) {
        Bindings bindings = engine.getBindings(scope);
        bindingsConsumer.accept(bindings);
        return setBindings(bindings, scope);
    }

    public ScriptEngineBuilder setVar(String key, Object value) {
        return setScopeVar(ScriptContext.ENGINE_SCOPE, key, value);
    }

    public ScriptEngineBuilder setGlobalVar(String key, Object value) {
        return setScopeVar(ScriptContext.GLOBAL_SCOPE, key, value);
    }

    public ScriptEngineBuilder setScopeVar(int scope, String key, Object value) {
        engine.getBindings(scope).put(key, value);
        return this;
    }

    public ScriptEngineBuilderWithResult eval(String script) {
        Object result = null;
        try {
            result = engine.eval(script);
            return new ScriptEngineBuilderWithResult(engine, result, null);
        } catch (ScriptException e) {
            return new ScriptEngineBuilderWithResult(engine, null, e);
        }
    }

    public ScriptEngineBuilderWithResult eval(Reader reader) {
        Object result = null;
        try {
            result = engine.eval(reader);
            return new ScriptEngineBuilderWithResult(engine, result, null);
        } catch (ScriptException e) {
            return new ScriptEngineBuilderWithResult(engine, null, e);
        }
    }

    public ScriptEngineBuilder getVar(String key, Consumer<Object> valueConsumer) {
        return getScopeVar(ScriptContext.ENGINE_SCOPE, key, valueConsumer);
    }

    public ScriptEngineBuilder getVar(String key, BiConsumer<String, Object> keyValueConsumer) {
        return getScopeVar(ScriptContext.ENGINE_SCOPE, key, keyValueConsumer);
    }

    public ScriptEngineBuilder getGlobalVar(String key, Consumer<Object> valueConsumer) {
        return getScopeVar(ScriptContext.GLOBAL_SCOPE, key, valueConsumer);
    }

    public ScriptEngineBuilder getGlobalVar(String key, BiConsumer<String, Object> keyValueConsumer) {
        return getScopeVar(ScriptContext.GLOBAL_SCOPE, key, keyValueConsumer);
    }

    public ScriptEngineBuilder getScopeVar(int scope, String key, Consumer<Object> valueConsumer) {
        Object value = engine.getBindings(scope).get(key);
        valueConsumer.accept(value);
        return this;
    }

    public ScriptEngineBuilder getScopeVar(int scope, String key, BiConsumer<String, Object> keyValueConsumer) {
        Object value = engine.getBindings(scope).get(key);
        keyValueConsumer.accept(key, value);
        return this;
    }

    public ScriptEngineBuilder getVars(BiConsumer<String, Object> keyValueConsumer) {
        return getScopeVars(ScriptContext.ENGINE_SCOPE, keyValueConsumer);
    }

    public ScriptEngineBuilder getGlobalVars(BiConsumer<String, Object> keyValueConsumer) {
        return getScopeVars(ScriptContext.GLOBAL_SCOPE, keyValueConsumer);
    }

    public ScriptEngineBuilder getScopeVars(int scope, BiConsumer<String, Object> keyValueConsumer) {
        Bindings bindings = engine.getBindings(scope);
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            keyValueConsumer.accept(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public ScriptEngine engine() {
        return engine;
    }

    public static class ScriptEngineBuilderWithResult extends ScriptEngineBuilder {
        private final Object result;
        private final ScriptException exception;

        ScriptEngineBuilderWithResult(ScriptEngine engine, Object result, ScriptException exception) {
            super(engine);

            this.result = result;
            this.exception = exception;
        }

        public ScriptEngineBuilderWithResult onResult(Consumer<Object> resultConsumer) {
            resultConsumer.accept(result);
            return this;
        }

        public ScriptEngineBuilderWithResult onResult(BiConsumer<ScriptEngine, Object> engineResultConsumer) {
            engineResultConsumer.accept(engine, result);
            return this;
        }

        public ScriptEngineBuilderWithResult onException(Consumer<ScriptException> exceptionConsumer) {
            if (exception != null) {
                exceptionConsumer.accept(exception);
            }
            return this;
        }

        public ScriptEngineBuilderWithResult onException(BiConsumer<ScriptEngine, ScriptException> engineExceptionConsumer) {
            if (exception != null) {
                engineExceptionConsumer.accept(engine, exception);
            }
            return this;
        }

        public Object result() {
            return result;
        }
    }
}
