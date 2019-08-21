package ch.obermuhlner.scriptengine.java;

import java.util.Map;

/**
 * A {@link ClassLoader} that loads classes from memory.
 */
public class MemoryClassLoader extends ClassLoader {

    private Map<String, byte[]> mapClassBytes;

    /**
     * Creates a {@link MemoryClassLoader}.
     *
     * @param mapClassBytes the map of class names to compiled classes
     * @param parent the parent {@link ClassLoader}
     */
    public MemoryClassLoader(Map<String, byte[]> mapClassBytes, ClassLoader parent) {
        super(parent);
        this.mapClassBytes = mapClassBytes;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        byte[] bytes = mapClassBytes.get(name);
        if (bytes == null) {
            return super.loadClass(name);
        }

        return defineClass(name, bytes, 0, bytes.length);
    }
}
