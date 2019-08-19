package ch.obermuhlner.scriptengine.java.constructor;

import ch.obermuhlner.scriptengine.java.internal.ReflectionUtil;

import javax.script.ScriptException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class DefaultConstructorStrategy implements ConstructorStrategy {

    private Class<?>[] argumentTypes;
    private final Object[] arguments;

    private DefaultConstructorStrategy(Class<?>[] argumentTypes, Object[] arguments) {
        this.argumentTypes = argumentTypes;
        this.arguments = arguments;
    }

    @Override
    public Object construct(Class<?> clazz) throws ScriptException {
        try {
            Constructor<?> constructor = findConstructor(clazz, argumentTypes, arguments);
            return constructor.newInstance(arguments);
        } catch (InstantiationException | IllegalAccessException |InvocationTargetException | NoSuchMethodException e) {
            throw new ScriptException(e);
        }
    }

    public static DefaultConstructorStrategy byDefaultConstructor() {
        return new DefaultConstructorStrategy(new Class<?>[0], new Object[0]);
    }

    public static DefaultConstructorStrategy byArgumentTypes(Class<?>[] argumentTypes, Object... arguments) {
        return new DefaultConstructorStrategy(argumentTypes, arguments);
    }

    public static DefaultConstructorStrategy byMatchingArguments(Object... arguments) {
        return new DefaultConstructorStrategy(null, arguments);
    }

    private Constructor<?> findConstructor(Class<?> clazz, Class<?>[] argumentTypes, Object[] arguments) throws NoSuchMethodException, ScriptException {
        if (argumentTypes != null) {
            return clazz.getConstructor(argumentTypes);
        }

        List<Constructor<?>> matchingConstructors = new ArrayList<>();
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (Modifier.isPublic(constructor.getModifiers())) {
                if (ReflectionUtil.matchesArguments(constructor, arguments)) {
                    matchingConstructors.add(constructor);
                }
            }
        }

        if (matchingConstructors.size() == 0) {
            throw new ScriptException("No constructor with matching arguments found");
        }
        if (matchingConstructors.size() > 1) {
            throw new ScriptException("Ambiguous constructors with matching arguments found: " + matchingConstructors.size());
        }

        return matchingConstructors.get(0);
    }
}
