package ch.obermuhlner.scriptengine.java.compilation;

import java.util.List;

import javax.tools.JavaFileObject;

/**
 * This strategy is used to decide what to compile
 */
public interface CompilationStrategy {

    /**
     * Generate a list of JavaFileObject to compile
     *
     * @param simpleClassName the class name of the script
     * @param currentSource   The current source script we want to execute
     * @return
     */
    List<JavaFileObject> getJavaFileObjectsToCompile(String simpleClassName, String currentSource);

    /**
     * As the script is compiled, this is an opportunity to see if we still want to
     * keep it.
     *
     * @param clazz
     */
    default void compilationResult(Class<?> clazz) {
    }

}
