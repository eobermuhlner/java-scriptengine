package ch.obermuhlner.scriptengine.java.constructor;

import javax.script.ScriptException;

public class NullConstructorStrategy implements ConstructorStrategy {
    @Override
    public Object construct(Class<?> clazz) throws ScriptException {
        return null;
    }
}
