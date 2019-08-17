package ch.obermuhlner.scriptengine.java.constructor;

import javax.script.ScriptException;
import java.lang.reflect.InvocationTargetException;

public class DefaultConstructorStrategy implements ConstructorStrategy {
    @Override
    public Object construct(Class<?> clazz) throws ScriptException {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |InvocationTargetException | NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }
}
