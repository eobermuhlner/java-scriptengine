package ch.obermuhlner.scriptengine.java.bindings;

import java.util.Map;

/**
 * The strategy used to set/get the Bindings around invoke
 */
public interface BindingStrategy {
	
	void associateBindings(Class<?> compiledClass, Object compiledInstance, Map<String, Object> mergedBindings);
	
	Map<String, Object> retrieveBindings(Class<?> compiledClass, Object compiledInstance);
}
