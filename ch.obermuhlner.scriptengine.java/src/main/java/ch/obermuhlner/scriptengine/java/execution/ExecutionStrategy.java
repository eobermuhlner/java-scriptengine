package ch.obermuhlner.scriptengine.java.execution;

import javax.script.ScriptException;

public interface ExecutionStrategy {
    Object execute(Object instance) throws ScriptException;
}
