package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;

public interface ExecutionStrategyFactory {
    public ExecutionStrategy create(Class<?> clazz) throws ScriptException;
}
