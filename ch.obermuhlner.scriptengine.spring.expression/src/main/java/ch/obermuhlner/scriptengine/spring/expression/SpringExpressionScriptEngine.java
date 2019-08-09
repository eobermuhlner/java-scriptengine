package ch.obermuhlner.scriptengine.spring.expression;

import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SpringExpressionScriptEngine implements ScriptEngine {
    public static final String ROOT = "_root";

    public static int BEAN_SCOPE = 150;

    private ScriptContext context = new SimpleScriptContext();

    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardEvaluationContext evaluationContext = new StandardEvaluationContext();


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
    public ScriptEngineFactory getFactory() {
        return new SpringExpressionScriptEngineFactory();
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
        Expression exp = parser.parseExpression(script);

        Bindings globalBindings = context.getBindings(ScriptContext.GLOBAL_SCOPE);
        Map<String, Object> variables = mergeBindings(globalBindings, bindings);
        evaluationContext.setVariables(variables);

        Object rootObject = variables.get(ROOT);

        Object result = exp.getValue(evaluationContext, rootObject);

        for (String key : variables.keySet()) {
            Object value = evaluationContext.lookupVariable(key);

            setBindingsValue(globalBindings, bindings, key, value);
        }

        return result;
    }

    private void setBindingsValue(Bindings globalBindings, Bindings engineBindings, String name, Object value) {
        if (!engineBindings.containsKey(name) && globalBindings.containsKey(name)) {
            globalBindings.put(name, value);
        } else {
            engineBindings.put(name, value);
        }
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

    private static class ScriptBeanResolver implements BeanResolver {
        @Override
        public Object resolve(EvaluationContext context, String beanName) throws AccessException {
            return null;
        }
    }
}
