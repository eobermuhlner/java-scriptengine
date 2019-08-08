package ch.obermuhlner.scriptengine.jshell;

import jdk.jshell.*;
import jdk.jshell.execution.DirectExecutionControl;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionControlProvider;
import jdk.jshell.spi.ExecutionEnv;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class JShellScriptEngine implements ScriptEngine {

    private ScriptContext context = new SimpleScriptContext();

    private final AccessDirectExecutionControl accessDirectExecutionControl = new AccessDirectExecutionControl();

    private final JShell jshell = JShell.builder()
            .executionEngine(new AccessDirectExecutionControlProvider(accessDirectExecutionControl), null)
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
        synchronized (jshell) {
            Bindings globalBindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
            pushVariables(globalBindings, bindings);

            Object result = evaluateScript(script);

            pullVariables(globalBindings, bindings);

            return result;
        }
    }

    private static Map<String, Object> staticVariables;
    private void pushVariables(Bindings globalBindings, Bindings engineBindings) throws ScriptException {
        staticVariables = mergeBindings(globalBindings, engineBindings);

        Set<String> remainingKeys = new HashSet<>(staticVariables.keySet());

        try {
            jshell.variables().forEach(varSnippet -> {
                String name = varSnippet.name();
                if (remainingKeys.contains(name)) {
                    remainingKeys.remove(name);
                    try {
                        Object value = getVariableValue(name);
                        String type = determineType(value);
                        String script = name + " = (" + type + ") " + getClass().getName() + ".getVariableValue(\"" + name + "\");";
                        evaluateScript(script);
                    } catch (ScriptException e) {
                        throw new ScriptRuntimeException(e);
                    }
                }
            });
        } catch (ScriptRuntimeException e) {
            throw (ScriptException) e.getCause();
        }

        for (String name : remainingKeys) {
            Object value = getVariableValue(name);
            String type = determineType(value);
            String script = "var " + name + " = (" + type + ") " + getClass().getName() + ".getVariableValue(\"" + name + "\");";
            evaluateScript(script);
        }
    }

    private void pullVariables(Bindings globalBindings, Bindings engineBindings) throws ScriptException {
        try {
            jshell.variables().forEach(varSnippet -> {
                String name = varSnippet.name();
                String script = getClass().getName() + ".setVariableValue(\"" + name + "\", " + name + ");";
                try {
                    evaluateScript(script);
                    Object value = getVariableValue(name);
                    setBindingsValue(globalBindings, engineBindings, name, value);
                } catch (ScriptException e) {
                    throw new ScriptRuntimeException(e);
                }
            });
        } catch (ScriptRuntimeException e) {
            throw (ScriptException) e.getCause();
        }
    }

    private void setBindingsValue(Bindings globalBindings, Bindings engineBindings, String name, Object value) {
        if (!engineBindings.containsKey(name) && globalBindings.containsKey(name)) {
            globalBindings.put(name, value);
        } else {
            engineBindings.put(name, value);
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
                    JShellException exception = event.exception();
                    String message = exception.getMessage() == null ? "" : ": " + exception.getMessage();
                    if (exception instanceof EvalException) {
                        EvalException evalException = (EvalException) exception;
                        throw new ScriptException(evalException.getExceptionClassName() + message + "\n" + event.snippet().source());
                    }
                    throw new ScriptException(message + "\n" + event.snippet().source());
                }

                if (event.status() == Snippet.Status.VALID) {
                    result = accessDirectExecutionControl.getLastValue();
                } else {
                    Snippet snippet = event.snippet();
                    Optional<Diag> optionalDiag = jshell.diagnostics(snippet).findAny();
                    if (optionalDiag.isPresent()) {
                        Diag diag = optionalDiag.get();
                        throw new ScriptException(diag.getMessage(null) + "\n" + completionInfo.source());
                    }

                    if (snippet instanceof DeclarationSnippet) {
                        DeclarationSnippet declarationSnippet = (DeclarationSnippet) snippet;
                        List<String> unresolvedDependencies = jshell.unresolvedDependencies(declarationSnippet).collect(Collectors.toList());
                        if (!unresolvedDependencies.isEmpty()) {
                            throw new ScriptException("Unresolved dependencies: " + unresolvedDependencies + "\n" + completionInfo.source());
                        }
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

    private static class ScriptRuntimeException extends RuntimeException {
        public ScriptRuntimeException(ScriptException cause) {
            super(cause);
        }
    }

    private static class AccessDirectExecutionControl extends DirectExecutionControl {
        private Object lastValue;

        @Override
        protected String invoke(Method doitMethod) throws Exception {
            lastValue = doitMethod.invoke(null, new Object[0]);
            return valueString(lastValue);
        }

        public Object getLastValue() {
            return lastValue;
        }
    }

    private static class AccessDirectExecutionControlProvider implements ExecutionControlProvider {
        private AccessDirectExecutionControl accessDirectExecutionControl;

        AccessDirectExecutionControlProvider(AccessDirectExecutionControl accessDirectExecutionControl) {
            this.accessDirectExecutionControl = accessDirectExecutionControl;
        }

        @Override
        public String name() {
            return "accessdirect";
        }

        @Override
        public ExecutionControl generate(ExecutionEnv env, Map<String, String> parameters) throws Throwable {
            return accessDirectExecutionControl;
        }
    }
}
