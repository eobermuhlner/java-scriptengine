package ch.obermuhlner.scriptengine.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DynamicClassLoader extends ClassLoader {

    private String dynamicClassName;
    private Path dynamicClassPath;

    DynamicClassLoader(String dynamicClassName, Path dynamicClassPath, ClassLoader parent) {
        super(parent);
        this.dynamicClassName = dynamicClassName;
        this.dynamicClassPath = dynamicClassPath;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!name.equals(dynamicClassName)) {
            return super.loadClass(name);
        }

        try {
            byte[] bytes = Files.readAllBytes(dynamicClassPath);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException("Class not found: " + name, e);
        }
    }
}
