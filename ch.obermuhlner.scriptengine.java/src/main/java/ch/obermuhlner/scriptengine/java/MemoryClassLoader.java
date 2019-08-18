package ch.obermuhlner.scriptengine.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MemoryClassLoader extends ClassLoader {

    private Map<String, byte[]> mapClassBytes;

    MemoryClassLoader(Map<String, byte[]> mapClassBytes, ClassLoader parent) {
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
