package ch.obermuhlner.scriptengine.java;

import jdk.jshell.*;
import jdk.jshell.execution.DirectExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import javax.script.*;
import javax.tools.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class JavaScriptEngine implements ScriptEngine, Compilable {

    private ScriptContext context = new SimpleScriptContext();

    @Override
    public ScriptContext getContext() {
        return context;
    }

    @Override
    public void setContext(ScriptContext context) {
        Objects.requireNonNull(context);
        this.context = context;
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public Bindings getBindings(int scope) {
        return context.getBindings(scope);
    }

    @Override
    public void setBindings(Bindings bindings, int scope) {
        context.setBindings(bindings, scope);
    }

    @Override
    public void put(String key, Object value) {
        getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
    }

    @Override
    public Object get(String key) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return eval(readScript(reader));
    }

    @Override
    public Object eval(String script) throws ScriptException {
        return eval(script, context);
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        return eval(readScript(reader), context);
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException {
        return eval(script, context.getBindings(ScriptContext.ENGINE_SCOPE));
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return eval(readScript(reader), bindings);
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        CompiledScript compile = compile(script);

        return compile.eval(bindings);
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        return compile(readScript(reader));
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        String simpleClassName = "Script";
        String fullClassName = simpleClassName; // example.Script
        String fileName = simpleClassName + ".class";

        JavaStringObject testSource = new JavaStringObject(simpleClassName, script);

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(testSource));
        if (!task.call()) {
            String message = diagnostics.getDiagnostics().stream()
                    .map(d -> d.toString())
                    .collect(Collectors.joining("\n"));
            throw new ScriptException(message);
        }

        DynamicClassLoader classLoader = new DynamicClassLoader(fullClassName, Path.of(fileName), JavaScriptEngine.class.getClassLoader());
        try {
            Class<?> clazz = classLoader.loadClass(fullClassName);
            Object instance = clazz.getDeclaredConstructor().newInstance();
            return new JavaCompiledScript(this, instance);
        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new ScriptException(e);
        }
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return new JavaScriptEngineFactory();
    }

    private String readScript(Reader reader) throws ScriptException {
        try {
            StringBuilder s = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                s.append(line);
                s.append("\n");
            }
            return s.toString();
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    public static void main(String[] args) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        JavaStringObject testSource = new JavaStringObject("Test", "" +
                "package example;" +
                "public class Test implements java.util.function.Supplier<String> {" +
                "   public String get() {" +
                "       return \"Hello\";" +
                "   } " +
                "}");

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, Arrays.asList(testSource));
        if (!task.call()) {
            diagnostics.getDiagnostics().forEach(System.out::println);
        }

        DynamicClassLoader classLoader = new DynamicClassLoader("example.Test", Path.of("Test.class"), JavaScriptEngine.class.getClassLoader());
        try {
            Class<?> clazz = classLoader.loadClass("example.Test");
            Object testInstance = clazz.getDeclaredConstructor().newInstance();
            if (testInstance instanceof Supplier) {
                Supplier testSupplier = (Supplier) testInstance;
                System.out.println(testSupplier.get());
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
