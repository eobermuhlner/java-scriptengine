package ch.obermuhlner.scriptengine.java.constructor;

import javax.script.ScriptException;

public interface ConstructorStrategy {
    Object construct(Class<?> clazz) throws ScriptException;
}
