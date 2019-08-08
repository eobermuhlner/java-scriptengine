package ch.obermuhlner.scriptengine.jshell;

import jdk.jshell.*;
import jdk.jshell.execution.LocalExecutionControlProvider;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class JShellScriptEngine implements ScriptEngine {
    private ScriptContext context = new SimpleScriptContext();
    private final JShell jshell = JShell.builder()
            .executionEngine(new LocalExecutionControlProvider(), null)
            .build();

    @Override
    public ScriptContext getContext() {
        return context;
    }

    @Override
    public void setContext(ScriptContext context) {
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
    public ScriptEngineFactory getFactory() {
        return new JShellScriptEngineFactory();
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
        Bindings globalBindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        Map<String, Object> variables = mergeBindings(globalBindings, bindings);
        pushVariables(variables);

        Object result = evaluateScript(script);

        return result;
    }

    private static Map<String, Object> staticVariables;
    private void pushVariables(Map<String, Object> variables) throws ScriptException {
        staticVariables = variables;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            String type = determineType(value);
            String script = "var " + name + " = (" + type + ") " + getClass().getName() + ".getVariableValue(\"" + name + "\");";
            evaluateScript(script);
        }
    }

    private String determineType(Object value) {
        if (value == null) {
            return Object.class.getCanonicalName();
        }

        String type = value.getClass().getCanonicalName();

        if (type == null) {
            type = Object.class.getCanonicalName();
        }

        return type;
    }

    public static Object getVariableValue(String name) {
        return staticVariables.get(name);
    }

    public static void setVariableValue(String name, Object value) {
        staticVariables.put(name, value);
    }

    private Object evaluateScript(String script) throws ScriptException {
        Object result = null;

        while (!script.isEmpty()) {
            SourceCodeAnalysis.CompletionInfo completionInfo = jshell.sourceCodeAnalysis().analyzeCompletion(script);
            if (!completionInfo.completeness().isComplete()) {
                throw new ScriptException("Incomplete script\n" + script);
            }

            List<SnippetEvent> events = jshell.eval(completionInfo.source());

            for (SnippetEvent event : events) {
                if (event.exception() != null) {
                    throw new ScriptException(event.exception());
                }

                if (event.status() == Snippet.Status.VALID) {
                    result = event.value(); // TODO convert value
                } else {
                    Optional<Diag> optionalDiag = jshell.diagnostics(event.snippet())
                            .findAny();
                    if (optionalDiag.isPresent()) {
                        Diag diag = optionalDiag.get();
                        throw new ScriptException(diag.getMessage(null) + "\n" + completionInfo.source());
                    }
                    throw new ScriptException("Unknown error\n" + completionInfo.source());
                }
            }

            script = completionInfo.remaining();
        }

        return result;
    }

    private Map<String, Object> mergeBindings(Bindings... bindingsToMerge) {
        Map<String, Object> variables = new HashMap<>();

        for (Bindings bindings : bindingsToMerge) {
            if (bindings != null) {
                for (Map.Entry<String, Object> globalEntry : bindings.entrySet()) {
                    variables.put(globalEntry.getKey(), globalEntry.getValue());
                }
            }
        }

        return variables;
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
}
