package ch.obermuhlner.scriptengine.java;

import ch.obermuhlner.scriptengine.java.execution.AutoExecutionStrategy;
import ch.obermuhlner.scriptengine.java.execution.ExecutionStrategy;

import javax.script.*;

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

        return executionStrategy.execute(instance);
    }

}
